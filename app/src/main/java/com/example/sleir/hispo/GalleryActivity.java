package com.example.sleir.hispo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.ViewPager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import junit.framework.Assert;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class GalleryActivity extends AppCompatActivity {
    public static final String TAG = "GalleryActivity";
    public static final String EXTRA_NAME = "images";
    public static final String SNIPPET = "snippet";
    public static final String TITLE = "title";
    public static final String DISTANCE = "distance";
    public static final String TYPE = "type";

    private ArrayList<String> _images;
    private GalleryPagerAdapter _adapter;

    @InjectView(R.id.pager) ViewPager _pager;
    @InjectView(R.id.thumbnails) LinearLayout _thumbnails;
    @InjectView(R.id.btn_close) ImageButton _closeButton;
    @InjectView(R.id.textView) TextView _textView;
    @InjectView(R.id.titleText) TextView _titleText;
    @InjectView(R.id.distanceText) TextView _distanceText;
    @InjectView(R.id.imageView) ImageView _markerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_gallery);
        ButterKnife.inject(this);


        _images = (ArrayList<String>) getIntent().getSerializableExtra(EXTRA_NAME);
        Assert.assertNotNull(_images);

        _adapter = new GalleryPagerAdapter(this);
        _pager.setAdapter(_adapter);
        _pager.setOffscreenPageLimit(6); // how many images to load into memory


        _textView.setText((String)getIntent().getSerializableExtra(SNIPPET));
        _textView.setTextSize(14);
        _textView.setMovementMethod(new ScrollingMovementMethod());

        _titleText.setText((String)getIntent().getSerializableExtra(TITLE));
        _titleText.setTextSize(20);
        _titleText.setGravity(Gravity.CENTER_VERTICAL);

        _distanceText.setText((String)getIntent().getSerializableExtra(DISTANCE));

        if(getIntent().getSerializableExtra(TYPE).equals("building")) {
            _markerView.setImageResource(R.mipmap.orapin);
            _titleText.setBackgroundColor(Color.parseColor("#f18039"));
        }
        else  if(getIntent().getSerializableExtra(TYPE).equals("ruin")) {
            _markerView.setImageResource(R.mipmap.blupin);
            _titleText.setBackgroundColor(Color.parseColor("#39aaf1"));
        }
        else  if(getIntent().getSerializableExtra(TYPE).equals("statue")) {
            _markerView.setImageResource(R.mipmap.grepin);
            _titleText.setBackgroundColor(Color.parseColor("#45cea2"));
        }
        else {
            _markerView.setImageResource(R.mipmap.rospin);
            _titleText.setBackgroundColor(Color.parseColor("#f079a9"));
        }
        //markerView.setImageResource(R.mipmap.greypin);

        _closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Close clicked");
                finish();
            }
        });
    }

    class GalleryPagerAdapter extends PagerAdapter {

        Context _context;
        LayoutInflater _inflater;

        public GalleryPagerAdapter(Context context) {
            _context = context;
            _inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return _images.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View itemView = _inflater.inflate(R.layout.pager_gallery_item, container, false);
            container.addView(itemView);

            // Get the border size to show around each image
            int borderSize = _thumbnails.getPaddingTop();

            // Get the size of the actual thumbnail image
            int thumbnailSize = ((FrameLayout.LayoutParams)
                    _pager.getLayoutParams()).bottomMargin - (borderSize*2);

            // Set the thumbnail layout parameters. Adjust as required
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(thumbnailSize, thumbnailSize);
            params.setMargins(0, 0, borderSize, 0);

            // You could also set like so to remove borders
            //ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
            //        ViewGroup.LayoutParams.WRAP_CONTENT,
            //        ViewGroup.LayoutParams.WRAP_CONTENT);

            final ImageView thumbView = new ImageView(_context);
            thumbView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumbView.setLayoutParams(params);
            thumbView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Thumbnail clicked");

                    // Set the pager position when thumbnail clicked
                    _pager.setCurrentItem(position);
                }
            });
            _thumbnails.addView(thumbView);

            final SubsamplingScaleImageView imageView =
                    itemView.findViewById(R.id.image);

            // Asynchronously load the image and set the thumbnail and pager view
            Glide.with(_context)
                    .load(_images.get(position))
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                            imageView.setImage(ImageSource.bitmap(bitmap));
                            thumbView.setImageBitmap(bitmap);
                        }
                    });

            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }
    }
}