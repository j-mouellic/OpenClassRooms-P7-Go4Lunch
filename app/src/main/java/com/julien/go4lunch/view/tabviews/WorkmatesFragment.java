package com.julien.go4lunch.view.tabviews;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.julien.go4lunch.R;
import com.julien.go4lunch.model.bo.Lunch;
import com.julien.go4lunch.model.bo.Workmate;
import com.julien.go4lunch.viewmodel.MyViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Fragment for displaying a list of workmates and their lunch status.
 * It observes the data from a ViewModel and updates a RecyclerView accordingly.
 */
public class WorkmatesFragment extends Fragment {

    // UTILS
    private final String TAG = "WorkmatesFragment";
    private MyViewModel viewModel;
    private TabActivity parentActivity;
    private List<WorkmateLunchPair> currentWorkmatesList;

    // VIEWS
    private WorkmateAdapter adapter;
    private TextInputEditText workmatesSearchInput;

    // CONSTRUCTORS
    public WorkmatesFragment(MyViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public WorkmatesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workmates, container, false);

        parentActivity = (TabActivity) getActivity();
        workmatesSearchInput = parentActivity.workmatesSearchInput;

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new WorkmateAdapter();
        recyclerView.setAdapter(adapter);

        refreshRecyclerView();
        configureWorkmatesSearchInput();

        return view;
    }

    /**
     * Refreshes the RecyclerView by observing data from the ViewModel.
     * Combines each workmate with their corresponding lunch (if available) and updates the adapter.
     */
    private void refreshRecyclerView() {
        viewModel.getAllWorkmates().observe(getViewLifecycleOwner(), workmates -> {

            viewModel.fetchTodayLunches().observe(getViewLifecycleOwner(), lunches -> {

                Map<Workmate, Lunch> lunchMap = new HashMap<>();

                // Associate lunch & workmate
                for (Lunch lunch : lunches) {
                    Log.i(TAG, "Workmate : " + lunch.getWorkmate());
                    lunchMap.put(lunch.getWorkmate(), lunch);
                }

                Log.i(TAG, "Lunch map size " + lunchMap.size());

                List<WorkmateLunchPair> pairs = new ArrayList<>();

                // Check if workmate is associated to a lunch ? lunch : null;
                for (Workmate workmate : workmates){
                    Lunch associatedLunch = lunchMap.get(workmate);
                    WorkmateLunchPair pair = new WorkmateLunchPair(workmate, associatedLunch);
                    pairs.add(pair);
                }

                currentWorkmatesList = pairs;

                adapter.updateWorkmatesList(pairs);
            });
        });
    }

    /**
     * Configures the search input for searching workmates by name.
     * When the user presses the search action on the keyboard, the list of workmates is filtered
     * based on the entered search term, and the results are updated in the adapter.
     */
    private void configureWorkmatesSearchInput(){
        workmatesSearchInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String search = view.getText().toString().toLowerCase();

                // Stream - Filter workmate and lunch list by workmate name and return a filtered workmate list
                List<WorkmateLunchPair> workmatesFound = currentWorkmatesList.stream()
                                .filter(workmateLunchPair -> workmateLunchPair.getWorkmate().getName().toLowerCase().contains(search))
                                        .collect(Collectors.toList());

                if (workmatesFound.size() > 0){
                    Log.i(TAG, workmatesFound.size() + " workmates found for search " + search);
                    adapter.updateWorkmatesList(workmatesFound);
                }else{
                    Log.i(TAG, workmatesFound.size() + " found for search " + search);
                    Toast.makeText(getContext(), "No Workmate Found", Toast.LENGTH_LONG).show();
                }

                parentActivity.resetTopBarViews();
                closeIME(view);

                return true;
            }
            return true;
        });
    }

    /**
     * Closes the soft keyboard (Input Method Editor - IME) by hiding it.
     *
     * This method is called to hide the soft keyboard after a search action is performed or when the user
     * finishes entering text in the search input.
     */
    private void closeIME(View view){
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Data class for pairing a workmate with their lunch information.
     */
    public class WorkmateLunchPair {

        /**
         * The workmate associated with this pair.
         */
        private final Workmate workmate;

        /**
         * The lunch associated with this pair (can be null if no lunch is selected).
         */
        private final Lunch lunch;

        /**
         * Constructor for creating a WorkmateLunchPair.
         */
        public WorkmateLunchPair(Workmate workmate, Lunch lunch) {
            this.workmate = workmate;
            this.lunch = lunch;
        }

        /**
         * Gets the workmate in this pair.
         */
        public Workmate getWorkmate() {
            return workmate;
        }

        /**
         * Gets the lunch in this pair.
         */
        public Lunch getLunch() {
            return lunch;
        }
    }
}