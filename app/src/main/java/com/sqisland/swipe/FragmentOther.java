package com.sqisland.swipe;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by oleh on 13-Nov-15.
 */
public class FragmentOther extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_other, container, false);

        RecyclerView shareApplicationList = (RecyclerView) rootView.findViewById(R.id.shareApplicationList);
        shareApplicationList.addItemDecoration(new DividerItemDecoration(getActivity(), null, true, true));

        ShareApplicationAdapter adapter = new ShareApplicationAdapter(getActivity());
        shareApplicationList.setAdapter(adapter);
        shareApplicationList.setLayoutManager(new LinearLayoutManager(getActivity()));


        Resources resources = getResources();
        int offsetPx;
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            offsetPx = resources.getDimensionPixelSize(resourceId);
        }else{
            offsetPx = (int)getResources().getDimension(R.dimen.bottom_offset_dp);
        }

        ServingClass.BottomOffsetDecoration bottomOffsetDecoration = new ServingClass.BottomOffsetDecoration(offsetPx);
        shareApplicationList.addItemDecoration(bottomOffsetDecoration);

        return rootView;
    }

}
