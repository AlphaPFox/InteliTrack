package br.gov.dpf.intelitrack.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import br.gov.dpf.intelitrack.R;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by andre.arc on 10/10/2018.
 */

public class CheckList extends LinearLayout
{
    //Bind views
    @BindView(R.id.txtStepNumber) TextView txtStepNumber;
    @BindView(R.id.txtStepDescription) TextView txtStepDescription;
    @BindView(R.id.imgStepStatus) ImageView imgStepStatus;
    @BindView(R.id.pgrStepProgress) ProgressBar pgrStepProgress;

    public CheckList(Context context) {
        this(context,null);
    }

    public CheckList(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CheckList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //Initialize component
        init();

        //Get properties from layout attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CheckList, 0, 0);

        //Set text values from attributes
        txtStepNumber.setText(a.getString(R.styleable.CheckList_stepNumber));
        txtStepDescription.setText(a.getString(R.styleable.CheckList_stepDescription));
        txtStepNumber.setTextColor(Color.parseColor(a.getString(R.styleable.CheckList_stepColor)));

        //Set indeterminate color
        pgrStepProgress.getIndeterminateDrawable().setColorFilter(Color.parseColor("#759CBB"), android.graphics.PorterDuff.Mode.SRC_IN);

        //Recycle array
        a.recycle();
    }

    /**
     * Initialize view
     */
    private void init()
    {
        //Inflate xml resource, pass "this" as the parent
        View root = inflate(getContext(),R.layout.checklist,this);

        //Get references to text views
        ButterKnife.bind(this, root);

        //Initially all views are gone
        imgStepStatus.setVisibility(GONE);
        pgrStepProgress.setVisibility(GONE);
        txtStepNumber.setVisibility(VISIBLE);
    }

    public void startProgress()
    {
        //Hide step number and status image
        txtStepNumber.setVisibility(GONE);
        imgStepStatus.setVisibility(GONE);

        //Show indeterminate progress bar
        pgrStepProgress.setVisibility(VISIBLE);
    }


    public void setResult(Drawable drawable)
    {
        //Hide indeterminate progress bar
        pgrStepProgress.setVisibility(GONE);

        //Show status image
        imgStepStatus.setImageDrawable(drawable);
        imgStepStatus.setVisibility(VISIBLE);
    }

    public void setDefault()
    {
        //Hide indeterminate progress bar and status image
        pgrStepProgress.setVisibility(GONE);
        imgStepStatus.setVisibility(GONE);

        //Show step number
        txtStepNumber.setVisibility(VISIBLE);
    }

    public boolean isRunning()
    {
        return pgrStepProgress.getVisibility() == VISIBLE;
    }
}
