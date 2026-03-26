package com.nsa.hafizq;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RecallFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecallFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private AnimatorSet pulseSet;
    private View playButton;


    public RecallFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment recall_layout.
     */
    // TODO: Rename and change types and number of parameters
    public static RecallFragment newInstance(String param1, String param2) {
        RecallFragment fragment = new RecallFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recall, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        playButton = view.findViewById(R.id.btn_play_recall);
        // 1. Find the button using the 'view' provided by the fragment
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 3. Use requireContext() instead of MainActivity.this
                Intent i = new Intent(requireContext(), RecallGameActivity.class);
                startActivity(i);
            }
        });

        startPlayButtonAnimation();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (pulseSet != null) {
            pulseSet.cancel(); // Stops the animation immediately
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (pulseSet != null) {
            pulseSet.start();
        } else {
            startPlayButtonAnimation();
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pulseSet != null) {
            pulseSet.cancel(); // Always clean up animations when fragment view is destroyed
        }
    }
    private void startPlayButtonAnimation(){

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(playButton, "scaleX", 1f, 1.5f, 1f);
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(playButton, "scaleY", 1f, 1.5f, 1f);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        ObjectAnimator elevation = ObjectAnimator.ofFloat(playButton, "translationZ", 0f, 1000f, 0f);
        elevation.setRepeatCount(ObjectAnimator.INFINITE);

        pulseSet = new AnimatorSet();
        pulseSet.playTogether(scaleX, scaleY, elevation);
        pulseSet.setDuration(3000);
        pulseSet.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseSet.start();

    }
}