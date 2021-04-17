package com.example.wings.mainactivity.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wings.R;
import com.example.wings.models.User;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseUser;

import static com.parse.ParseObject.create;

//All auto-filled stuff, just follow the samples I left behind!


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String TAG = "UserProfileFragment";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;



    public UserProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserProfileFragment newInstance(String param1, String param2) {
        UserProfileFragment fragment = new UserProfileFragment();
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
        return inflater.inflate(R.layout.fragment_user_profile, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

      //temp for testing
        //Won't let us use User methods
//        Object user = create(User.class);
        //ParseObject cant be cast to user
//        ParseObject user = new User();
//        User user = ParseObject.create(User.class);*/

        //You must create this type of ParseObject using ParseObject.create() or the proper subclass.
//        User user = new User();

//        User user = ParseObject.createWithoutData(User.class, "username");
//        user.setEmail("lmsiu@cpp.edu");
//        user.setFirstName("Laura");
//        user.setLastName("Siu");
//        user.setProfileSetUp(false);
//        user.setRating(5);
//        user.setPin(1111);


//        Log.i(TAG, "username: " + user.getUsername() + " FirstName: " + user.getFirstName() + " Last Name: " + user.getLastName() + " Email: " + user.getEmail() + " Pin: " + user.getPin());




    }
}