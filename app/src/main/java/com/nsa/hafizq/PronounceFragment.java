package com.nsa.hafizq;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PronounceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PronounceFragment extends Fragment {
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private AnimatorSet pulseSet;
    private ImageButton playWaveButton;



    public PronounceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PronounceFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PronounceFragment newInstance(String param1, String param2) {
        PronounceFragment fragment = new PronounceFragment();
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
        return inflater.inflate(R.layout.fragment_pronounce, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        playWaveButton = view.findViewById(R.id.btn_play_wave);
        // 1. Find the button using the 'view' provided by the fragment
//        playGuessButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // 3. Use requireContext() instead of MainActivity.this
//                Intent i = new Intent(requireContext(), GuessGameActivity.class);
//                startActivity(i);
//            }
//        });

        startPlayButtonAnimation();
        playWaveButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    // 1. Push Down: Shrink slightly and lower elevation
                    v.animate().scaleX(0.92f).scaleY(0.92f).translationZ(2f).setDuration(100).start();
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:

                    // 2. Pop Back: Return to normal size and original elevation
                    v.animate().scaleX(1f).scaleY(1f).translationZ(12f).setDuration(100).start();

                    // 3. Trigger the Actual Click Action
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        handlePlayClick();
                    }
                    break;
            }
            return true;
        });
        view.findViewById(R.id.button_guess_fragment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToFragment(GuessFragment.class);
            }
        });
    }

    private void goToFragment(Class<? extends Fragment> newFragmentClass) {
        if (getActivity() == null) return;
        ((MainActivity) requireActivity()).switchFragment(newFragmentClass);
    }
    private void handlePlayClick(){
//        if (pulseSet != null) pulseSet.cancel();
        // 3. Use requireContext() instead of MainActivity.this
        Intent i = new Intent(requireContext(), KalimahWaveGameActivity.class);
        startActivity(i);
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

    private void startPlayButtonAnimation() {
        // 1. Subtle Scaling (1.5f is too much, it might look blurry. 1.1f is "classy")
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(playWaveButton, "scaleX", 1f, 1.1f, 1f);
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(playWaveButton, "scaleY", 1f, 1.1f, 1f);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);

        // 2. Realistic 3D Elevation (Moving from 4dp to 20dp creates a "floating" effect)
        // Note: Ensure your button has a background that supports shadows (like a MaterialButton or a shape)
        ObjectAnimator elevation = ObjectAnimator.ofFloat(playWaveButton, "translationZ", 4f, 20f, 4f);
        elevation.setRepeatCount(ObjectAnimator.INFINITE);

        pulseSet = new AnimatorSet();
        pulseSet.playTogether(scaleX, scaleY, elevation);

        // 3. Timing (3000ms is a nice, calm "breathing" pace)
        pulseSet.setDuration(3000);
        pulseSet.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseSet.start();
    }


}