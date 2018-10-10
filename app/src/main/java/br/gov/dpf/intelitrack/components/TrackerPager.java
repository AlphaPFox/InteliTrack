package br.gov.dpf.intelitrack.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import br.gov.dpf.intelitrack.R;

public class TrackerPager extends FrameLayout
{
    //Main component from this custom view
    private ViewPager trackerPager;

    public TrackerPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        //Load layout from XML
        View root = inflate(context, R.layout.tracker_pager, this);

        //Find view pager
        trackerPager = root.findViewById(R.id.trackerPager);

        //Previous button event
        root.findViewById(R.id.left_nav).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousPage();
            }
        });

        //Next button event
        root.findViewById(R.id.right_nav).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPage();
            }
        });


        //Load tracker list
        ImagePagerAdapter adapter = new ImagePagerAdapter();

        //Apply list to view pager
        trackerPager.setAdapter(adapter);

    }

    public String getSelectedModel()
    {
        //Get adapter from pager
        ImagePagerAdapter adapter = (ImagePagerAdapter) trackerPager.getAdapter();

        //Return selected model
        return adapter.models[trackerPager.getCurrentItem()];
    }

    private void nextPage() {
        int currentPage = trackerPager.getCurrentItem();
        int totalPages = trackerPager.getAdapter().getCount();

        int nextPage = currentPage+1;
        if (nextPage >= totalPages) {
            // We can't go forward anymore.
            // Loop to the first page. If you don't want looping just
            // return here.
            nextPage = 0;
        }

        trackerPager.setCurrentItem(nextPage, true);
    }

    private void previousPage() {
        int currentPage = trackerPager.getCurrentItem();
        int totalPages = trackerPager.getAdapter().getCount();

        int previousPage = currentPage-1;
        if (previousPage < 0) {
            // We can't go back anymore.
            // Loop to the last page. If you don't want looping just
            // return here.
            previousPage = totalPages - 1;
        }

        trackerPager.setCurrentItem(previousPage, true);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        requestDisallowInterceptTouchEvent(true);
        return super.onInterceptTouchEvent(event);

    }

    private class ImagePagerAdapter extends PagerAdapter {
        private int[] mImages = new int[] {
                R.drawable.model_tk102b,
                R.drawable.model_pt39,
                R.drawable.model_spot,
                R.drawable.model_tk306,
                R.drawable.model_st940,
                R.drawable.model_gt02
        };

        private String[] mTitles = new String[] {
                "PowerPack TK102B",
                "TechGPS PT-39",
                "Spot TRACE",
                "Coban TK306",
                "Suntech ST940",
                "Coban GT-02"
        };

        private String[] models = new String[] {
                "tk102b",
                "pt39",
                "spot",
                "tk306",
                "st940",
                "gt02"
        };

        @Override
        public int getCount() {
            return mImages.length;
        }


        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Context context = getContext();

            //Define viewpager root, linear layout
            LinearLayout layout = new LinearLayout(context);
            layout.setGravity(Gravity.CENTER);
            layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            layout.setOrientation(LinearLayout.VERTICAL);

            //View pager header, text view
            TextView textView = new TextView(context);
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setText(mTitles[position]);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            textView.setTypeface(null, Typeface.BOLD);
            textView.setGravity(Gravity.CENTER);

            //View pager image, image view
            ImageView imageView = new ImageView(context);
            int padding = context.getResources().getDimensionPixelSize(R.dimen.padding_image);
            imageView.setPadding(padding, padding, padding, padding);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setImageResource(mImages[position]);

            //Add view to layout
            layout.addView(textView, 0);
            layout.addView(imageView, 1);
            container.addView(layout);

            //Return view
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }


    }

}
