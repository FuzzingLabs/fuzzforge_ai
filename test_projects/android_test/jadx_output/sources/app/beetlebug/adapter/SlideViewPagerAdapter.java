package app.beetlebug.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.viewpager.widget.PagerAdapter;
import app.beetlebug.C0572R;
import app.beetlebug.Walkthrough;
import app.beetlebug.user.UserSignUp;

/* loaded from: classes5.dex */
public class SlideViewPagerAdapter extends PagerAdapter {
    Context ctx;
    Button nextSlide;

    public SlideViewPagerAdapter(Context ctx) {
        this.ctx = ctx;
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public int getCount() {
        return 2;
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public Object instantiateItem(ViewGroup container, final int position) {
        LayoutInflater layoutInflater = (LayoutInflater) this.ctx.getSystemService("layout_inflater");
        View view = layoutInflater.inflate(C0572R.layout.slide_screen, container, false);
        ImageView imageView = (ImageView) view.findViewById(C0572R.id.imageViewSliderImage);
        ImageView ind1 = (ImageView) view.findViewById(C0572R.id.indicator1);
        ImageView ind2 = (ImageView) view.findViewById(C0572R.id.indicator2);
        TextView title = (TextView) view.findViewById(C0572R.id.textViewSliderTitle);
        Button gotoMain = (Button) view.findViewById(C0572R.id.buttonSliderButton);
        Button nextSlide = (Button) view.findViewById(C0572R.id.buttonSliderButtonNext);
        gotoMain.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.adapter.SlideViewPagerAdapter.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent intent = new Intent(SlideViewPagerAdapter.this.ctx, (Class<?>) UserSignUp.class);
                intent.setFlags(268468224);
                SlideViewPagerAdapter.this.ctx.startActivity(intent);
            }
        });
        nextSlide.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.adapter.SlideViewPagerAdapter.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Walkthrough.viewPager.setCurrentItem(position + 1);
            }
        });
        switch (position) {
            case 0:
                imageView.setImageResource(C0572R.drawable.walkthrough_one);
                ind1.setImageResource(C0572R.drawable.selected_dot);
                ind2.setImageResource(C0572R.drawable.unselected_dot);
                title.setText(C0572R.string.onboarding_one);
                nextSlide.setVisibility(0);
                nextSlide.setText("Next");
                break;
            case 1:
                imageView.setImageResource(C0572R.drawable.walkthrough_two);
                ind1.setImageResource(C0572R.drawable.unselected_dot);
                ind2.setImageResource(C0572R.drawable.selected_dot);
                title.setText(C0572R.string.onboarding_two);
                nextSlide.setVisibility(8);
                gotoMain.setText("Start Capture");
                break;
        }
        container.addView(view);
        return view;
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
