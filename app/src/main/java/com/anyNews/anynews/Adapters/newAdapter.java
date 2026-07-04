//package com.anyNews.anynews.Adapters;
//
//import android.app.Activity;
//import android.app.Dialog;
//import android.app.ProgressDialog;
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.SurfaceTexture;
//import android.graphics.drawable.Drawable;
//import android.media.AudioManager;
//import android.os.Build;
//import android.view.LayoutInflater;
//import android.view.TextureView;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.WindowManager;
//import android.webkit.WebSettings;
//import android.webkit.WebView;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ProgressBar;
//import android.widget.RelativeLayout;
//import android.widget.SeekBar;
//import android.widget.TextView;
//import android.widget.Toast;
//import android.widget.VideoView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.RequiresApi;
//import androidx.cardview.widget.CardView;
//import androidx.recyclerview.widget.RecyclerView;
//import androidx.viewpager.widget.ViewPager;
//
//import com.anyNews.anynews.Pojo.CommentResponuse;
//import com.anyNews.anynews.Pojo.EncreptionRequest;
//import com.anyNews.anynews.Pojo.LikeDownloadRespouse;
//import com.anyNews.anynews.Pojo.ListAllArticlesResponse;
//import com.anyNews.anynews.Pojo.ListOfCommentsRequest;
//import com.anyNews.anynews.Pojo.ListOfCommentsResponse;
//import com.anyNews.anynews.Pojo.Splash;
//import com.anyNews.anynews.R;
//import com.anyNews.anynews.Services.APIService;
//import com.anyNews.anynews.Services.RestApiClient;
//import com.anyNews.anynews.Services.ShadowTransformer;
//import com.anyNews.anynews.Utils.Allsharedpreference;
//import com.anyNews.anynews.Utils.CommonMethods;
//import com.anyNews.anynews.Utils.Constants;
//import com.anyNews.anynews.Utils.CustomMediaController;
//import com.anyNews.anynews.Utils.SharedPrefsData;
//import com.google.android.material.card.MaterialCardView;
//import com.google.android.youtube.player.YouTubePlayer;
//import com.google.android.youtube.player.YouTubePlayerView;
//import com.google.gson.Gson;
//import com.google.gson.JsonObject;
//
//import java.io.FileOutputStream;
//import java.util.List;
//import java.util.Random;
//
//import cn.jzvd.JzvdStd;
//import de.hdodenhof.circleimageview.CircleImageView;
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//import static com.anyNews.anynews.Utils.AppHelper.showLog;
//import static com.anyNews.anynews.Utils.MyUtils.decrypt;
//
//public class newAdapter extends RecyclerView.Adapter<newAdapter.ViewHolder> implements ViewPagerEx.OnPageChangeListener, TextureView.SurfaceTextureListener
//{
//    private final Random random = new Random();
//    private List<ListAllArticlesResponse.Datum> models;
//    private LayoutInflater layoutInflater;
//     Context context;
//     Allsharedpreference prefShare;
//     CommonMethods commonMethods;
//    String frameVideo = "";
//    Activity activity;
//    ViewGroup parent;
//    int serialNumber=0;
//    boolean click_event=true;
//    FileOutputStream outputStream=null;
//
//
//    Dialog dialog12;
//    ImageView image_delete;
//    RecyclerView mRecyclerView;
//    List<ListOfCommentsResponse.Datum> chooseLanguageModels;
//    int POST_ID=123;
//    TextView post_news_button;
//    EditText write_comment;
//    LinearLayout layoutLoading,resize_layout,linear_no_recyclerView;
//    TextView total_number_ofcomments;
//    CardView card_view;
//
//
//    YouTubePlayer youTubePlayer;
//    boolean wasRestored = false;
//
//    boolean isClosed = false;
//    private float x1,x2;
//    static final int MIN_DISTANCE = 150;
//    CustomMediaController mediaController;
//    AudioManager audioManager;
//    ProgressDialog pDialog;
//
//
//    // slider
//    private ViewPager mViewPager;
//    private CardPagerAdapter mCardAdapter;
//    private ShadowTransformer mCardShadowTransformer;
//    private TextView[] dots;
//
//     Runnable onEverySecond;
//
//    public newAdapter(List<ListAllArticlesResponse.Datum> models, Context context, Activity activity) {
//         audioManager = (AudioManager)context. getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
//
//        this.models = models;
//        this.context = context;
//        this.activity = activity;
//        prefShare = new Allsharedpreference(context);
//        commonMethods = new CommonMethods(activity);
//        this.activity.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
//                WindowManager.LayoutParams.MATCH_PARENT);
//        mediaController= new CustomMediaController(context);
//
//    }
//
//    @Override
//    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//
//   //     Toast.makeText(context, "Page Scrolled ", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onPageSelected(int position) {
//
//     //   Toast.makeText(context, "Page Selected ", Toast.LENGTH_SHORT).show();
//
//    }
//
//    @Override
//    public void onPageScrollStateChanged(int state) {
//
//   //     Toast.makeText(context, "Page Scroll State Changed ", Toast.LENGTH_SHORT).show();
//
//
//}
//
//    @Override
//    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
//
//        Toast.makeText(context, "onSurfaceTextureAvailable ", Toast.LENGTH_SHORT).show();
//
//    }
//
//    @Override
//    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
//
//        Toast.makeText(context, "onSurfaceTextureSizeChanged ", Toast.LENGTH_SHORT).show();
//
//    }
//
//    @Override
//    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
//
//
//        Toast.makeText(context, "onSurfaceTextureDestroyed ", Toast.LENGTH_SHORT).show();
//
//        return true;
//    }
//
//    @Override
//      public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
//
//    }
//
//
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        private TextView text;
//        ImageView imageView;
//        TextView txtTitle, txtDescription,txt_time;
//        ImageView adImg;
//        RelativeLayout relativeBottom, relative_videoview_parent;
//        RelativeLayout relative_adds,relative_main,parent_off_image;
//        LinearLayout layoutSlider;
//        SliderLayout sliderLayout;
//        LinearLayout comments_news;
//        final BaseSliderView sliderView=null;
//        LinearLayout image_download_gone,image_download_visible,share_linear,layoutScreenShot;
//        LinearLayout download_image,full_image_share,full_image_download,full_image_parent,liner_adImg;
//         CardView card_view;
//        WebView displayYoutubeVideo;
//        WebSettings webSettings;
//
//        Context mcontext;
//        //full image
//        LinearLayout full_image_whatsup;
//
//       // RelativeLayout relative_slider_full_image;
//
//
//
//
//        // half video details
//        RelativeLayout relative_half_video_parent;
//        YouTubePlayerView haff_video_view,video_view;
//        TextView half_video_text_Title,half_video_txt_time,half_video_txtDescription;
//        LinearLayout haff_video_comments_news,half_video_share_linear,half_video_download_image;
//
//        // full video
//        LinearLayout full_video_download,full_video_whatsup,full_video_share;
//        RelativeLayout full_video_image_parent;
//        TextView full_video_sound_icon;
//
//        // full video narmal
//        RelativeLayout full_video_player_view;
//        private VideoView videoView;
//        CircleImageView what_up_full_normal_video;
//        LinearLayout full_normal_video_comment_layout,full_normal_vedio_shared_layout,normal_full_video_download,full_normal_vedio_sound_unmute_layout,full_normal_vedio_sound_layout;
//        TextView full_video_normal_username;
//
//
//
//        MaterialCardView MaterialCardView;
//        // half video normal
//        RelativeLayout normal_half_video_parent;
//        LinearLayout normal_haff_video_comments_news,normal_half_video_share_linear,normal_half_video_download_image;
//        private  JzvdStd half_video_downloader;
//
//        // slider
//        RelativeLayout slider;
//        LinearLayout ll_dots;
//        ViewPager mViewPager;
//
//        // full video
//        LinearLayout side_menu;
//        LinearLayout linear_play_and_pause;
//        ImageView full_image_for_play_and_pause;
//        TextView full_test_for_play_and_pause;
//
//        ProgressBar progressBar = null;
//        ImageView full_video_thaumbnail;
//
//        SeekBar simpleSeekBar;
//        TextView time_for_full_video;
//
//        TextView share_test;
//        ImageView share_image_view;
//        public ViewHolder(View view,Context context) {
//            super(view);
//
//
//
//        }
//
//
//
//
//
//
//    }
//
//
//
//    @Override
//    public newAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//
//        this.parent = parent;
//        LayoutInflater layoutInflater=LayoutInflater.from(context);
//        View view=layoutInflater.inflate(R.layout.item, parent, false);
//        return new ViewHolder(view,context);
//
//    }
//
//
//
//
//
//    //@RequiresApi(api = Build.VERSION_CODES.M)
//    @Override
//    public void onBindViewHolder(ViewHolder v, final int position) {
//
///*
//
//          if (models.get(position).getPostType().equals("normal_image")) {
//
//
//            v.half_video_downloader.releaseAllVideos();
//
//            v.relative_main.setVisibility(View.VISIBLE);
//            v.relative_videoview_parent.setVisibility(View.GONE);
//            v.full_video_player_view.setVisibility(View.GONE);
//            v.relative_half_video_parent.setVisibility(View.GONE);
//            v.relative_adds.setVisibility(View.GONE);
//            v.normal_half_video_parent.setVisibility(View.GONE);
//            v.slider.setVisibility(View.GONE);
//            if (v.imageView != null) {
//
//                Glide.with(context).load(models.get(position).getImages().get(0)).into(v.imageView);
//
//            }else {
//
//                Picasso.with(context).load(R.drawable.login_icon).skipMemoryCache().into(v.imageView);
//
//            }
//
//        }
//
//          if (models.get(position).getPostType().equals("full_image")) {
//
//            v.half_video_downloader.releaseAllVideos();
//
//            if (models.get(position).getMediaType().equals("1")) {
//
//                v.relative_adds.setVisibility(View.VISIBLE);
//                v.relative_main.setVisibility(View.GONE);
//                v.relative_videoview_parent.setVisibility(View.GONE);
//                v.haff_video_view.setVisibility(View.GONE);
//                v.full_video_player_view.setVisibility(View.GONE);
//                v.normal_half_video_parent.setVisibility(View.GONE);
//                v.slider.setVisibility(View.GONE);
//                if(v.adImg !=null){
//
//                    Glide.with(context).load(models.get(position).getImages().get(0)).into(v.adImg);
//
//                }
//
//
//            }
//            if (models.get(position).getMediaType().equals("2")) {
//
//                v.relative_main.setVisibility(View.GONE);
//                v.relative_adds.setVisibility(View.GONE);
//                v.slider.setVisibility(View.VISIBLE);
//                v.full_video_player_view.setVisibility(View.GONE);
//                v.ll_dots.removeAllViews();
//                v.ll_dots.setVisibility(View.GONE);
//
//                mCardAdapter=null;
//
//                final ImageView img = new ImageView(context);
//                Picasso.with(img.getContext()).load(models.get(position).getBackgroundImage()).into(img, new com.squareup.picasso.Callback() {
//                    @Override
//                    public void onSuccess() {
//                        v.slider.setBackgroundDrawable(img.getDrawable());
//                    }
//
//                    @Override
//                    public void onError() {
//                    }
//                });
//
//                mCardAdapter = new CardPagerAdapter(context);
//
//
//                for(int i=0;i< models.get(position).getImages().size();i++){
//
//                    mCardAdapter.addCardItem(new CardItem(""+models.get(position).getImages().get(i),""+""+models.get(position).getImages().get(i)));
//
//                }
//
//
//                mCardShadowTransformer = new ShadowTransformer(v.mViewPager, mCardAdapter);
//                mCardShadowTransformer.enableScaling(true);
//                v.mViewPager.setAdapter(mCardAdapter);
//                v.ll_dots.setVisibility(View.VISIBLE);
//
//
//                dots = new TextView[mCardAdapter.getCount()];
//                for (int i = 0; i < dots.length; i++) {
//
//                    dots[i] = new TextView(context);
//                    dots[i].setText(Html.fromHtml("&#8226;"));
//                    dots[i].setTextSize(18);
//                    dots[i].setTextColor(Color.parseColor("#44715F5F"));
//                    v.ll_dots.addView(dots[i]);
//                }
//
//                if (dots.length > 0){
//
//                    dots[0].setTextColor(Color.parseColor("#FFFFFF"));
//                    dots[0].setTextSize(30);
//                }
//
//
//                v.mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//                    @Override
//                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//
//                    }
//
//                    @Override
//                    public void onPageSelected(int position) {
//
//                        try {
//
//                            v.ll_dots.removeAllViews();
//                            dots = new TextView[mCardAdapter.getCount()];
//                            for (int i = 0; i < dots.length; i++) {
//
//                                dots[i] = new TextView(context);
//                                dots[i].setText(Html.fromHtml("&#8226;"));
//                                dots[i].setTextSize(18);
//                                dots[i].setTextColor(Color.parseColor("#44715F5F"));
//                                v.ll_dots.addView(dots[i]);
//                            }
//
//                            if (dots.length > 0){
//                                dots[position].setTextColor(Color.parseColor("#FFFFFF"));
//                                dots[position].setTextSize(30);
//                            }
//
//                        }catch (ArrayIndexOutOfBoundsException e){
//
//                        }
//
//
//                    }
//
//                    @Override
//                    public void onPageScrollStateChanged(int state) {
//
//                    }
//                });
//
//            }
//        }
//          if (models.get(position).getPostType().equals("full_video")) {
//
//            if (models.get(position).getMediaType().equals("3")) {
//                int mCurrentPosition = 0;
//
//
//
//
//
//                v.linear_play_and_pause.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//                        if(v.full_test_for_play_and_pause.getText().equals("Play")){
//                            v.full_test_for_play_and_pause.setText("Pause");
//                            v.full_image_for_play_and_pause.setImageResource(R.drawable.ic_pause_video_button);
//
//                           v.videoView.start();
//                            v.simpleSeekBar.postDelayed(onEverySecond, 1000);
//
//
//
//                        }else {
//                            v.full_test_for_play_and_pause.setText("Play");
//                            v.full_image_for_play_and_pause.setImageResource(R.drawable.ic_play_button_video);
//
//                            v.videoView.pause();
//                         //
//                        }
//
//
//                    }
//                });
//                v.simpleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//
//                    @Override
//                    public void onStopTrackingTouch(SeekBar seekBar) {
//                    }
//
//                    @Override
//                    public void onStartTrackingTouch(SeekBar seekBar) {
//                    }
//
//                    @Override
//                    public void onProgressChanged(SeekBar seekBar, int progress,
//                                                  boolean fromUser) {
//
//                        if(fromUser) {
//                            // this is when actually seekbar has been seeked to a new position
//                            v.videoView.seekTo(progress);
//                        }
//                    }
//                });
//
//                onEverySecond=new Runnable() {
//
//                    @Override
//                    public void run() {
//
//                        if(v.simpleSeekBar != null) {
//                            v.simpleSeekBar.setProgress(v.videoView.getCurrentPosition());
//
//                            long minutes = ((v.videoView.getDuration()-v.videoView.getCurrentPosition()) / 1000) / 60;
//                            long seconds = ((v.videoView.getDuration()-v.videoView.getCurrentPosition())/ 1000) % 60;
//
//                            if(seconds>9){
//                                v.time_for_full_video.setText("0"+minutes+" : "+seconds);
//                            }else {
//                                v.time_for_full_video.setText("0"+minutes+" : 0"+seconds);
//                            }
//
//
//                        }
//
//                        if(v.videoView.isPlaying()) {
//                            v.simpleSeekBar.postDelayed(onEverySecond, 1000);
//                        }
//
//                    }
//                };
//
//                v.half_video_downloader.releaseAllVideos();
//
//
//
//                v.relative_main.setVisibility(View.GONE);
//                v.relative_adds.setVisibility(View.GONE);
//                v.relative_half_video_parent.setVisibility(View.GONE);
//                v.relative_videoview_parent.setVisibility(View.GONE);
//                v.video_view.setVisibility(View.GONE);
//                v.normal_half_video_parent.setVisibility(View.GONE);
//                v.full_video_player_view.setVisibility(View.VISIBLE);
//                v.slider.setVisibility(View.GONE);
//
//
//
//                v.progressBar.setVisibility(View.VISIBLE);
//                v.full_video_thaumbnail.setVisibility(View.VISIBLE);
//                v.side_menu.setVisibility(View.GONE);
//                v.full_video_normal_username.setVisibility(View.GONE);
//                v.simpleSeekBar.setVisibility(View.GONE);
//                v.linear_play_and_pause.setVisibility(View.GONE);
//
//
//
//
//
//
//
//
//                   Picasso.with(context).load(models.get(position).getThumbnail()).skipMemoryCache().into(v.full_video_thaumbnail);
//
//                   v.videoView.setVideoURI(Uri.parse(models.get(position).getVideoUrl()));
//
//                   v.videoView.setOnClickListener(new View.OnClickListener() {
//                       @Override
//                       public void onClick(View view) {
//                           if(v.side_menu.isShown()){
//                               v.side_menu.setVisibility(View.GONE);
//                               v.full_video_normal_username.setVisibility(View.GONE);
//                               v.simpleSeekBar.setVisibility(View.GONE);
//                               v.linear_play_and_pause.setVisibility(View.GONE);
//                           }else {
//                               v.side_menu.setVisibility(View.VISIBLE);
//                               v.full_video_normal_username.setVisibility(View.VISIBLE);
//                               v.simpleSeekBar.setVisibility(View.VISIBLE);
//                               v.linear_play_and_pause.setVisibility(View.VISIBLE);
//                           }
//
//                       }
//                   });
//
//                   v.videoView.setZOrderOnTop(true);//this line solve the problem
//
//                   v.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                       @Override
//                       public void onPrepared(MediaPlayer mediaPlayer) {
//                           if (mCurrentPosition > 0) {
//                               v.videoView.seekTo(mCurrentPosition);
//                           } else {
//                               // Skipping to 1 shows the first frame of the video.
//                               v.videoView.seekTo(1);
//                           }
//
//
//                           v.videoView.setZOrderOnTop(false);//this line solve the problem
//                           mediaPlayer.start();
//
//
//
//                           long minutes = (v.videoView.getDuration() / 1000) / 60;
//                           long seconds = (v.videoView.getDuration()/ 1000) % 60;
//
//                           if(seconds>9){
//                               v.time_for_full_video.setText("0"+minutes+" : "+seconds);
//                           }else {
//                               v.time_for_full_video.setText("0"+minutes+" : 0"+seconds);
//                           }
//
//                           float videoRatio = mediaPlayer.getVideoWidth() / (float) mediaPlayer.getVideoHeight();
//                           float screoRatio = v.videoView.getWidth() / (float) v.videoView.getHeight();
//
//                           float scale = videoRatio / screoRatio;
//
//                           if (scale >= 1f) {
//                               v.videoView.setScaleX(scale);
//                           } else {
//                               v.videoView.setScaleY(1f / scale);
//                           }
//
//
//
//                           v.simpleSeekBar.setMax(v.videoView.getDuration());
//                           v.simpleSeekBar.postDelayed(onEverySecond, 1000);
//
//
//                           mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
//                               @Override
//                               public void onVideoSizeChanged(MediaPlayer mp, int arg1, int arg2) {
//                                   v.side_menu.setVisibility(View.VISIBLE);
//                                   v.progressBar.setVisibility(View.GONE);
//                                   v.full_video_thaumbnail.setVisibility(View.GONE);
//                                   v.full_video_normal_username.setVisibility(View.VISIBLE);
//                                   v.simpleSeekBar.setVisibility(View.VISIBLE);
//                                   v.linear_play_and_pause.setVisibility(View.VISIBLE);
//
//
//                                   // TODO Auto-generated method stub
//                                   mp.start();
//
//                               }
//                           });
//
//                       }
//
//                   });
//
//
//                   v.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                       @Override
//                       public void onCompletion(MediaPlayer mediaPlayer) {
//
//                           v.videoView.start();
//                           v.videoView.setVisibility(View.VISIBLE);
//
//
//                       }
//                   });
//
//               }
//
//
//            if (models.get(position).getMediaType().equals("4")) {
//
//                v.half_video_downloader.releaseAllVideos();
//
//                v.relative_main.setVisibility(View.GONE);
//                v.relative_adds.setVisibility(View.GONE);
//                v.relative_half_video_parent.setVisibility(View.GONE);
//                v.full_video_player_view.setVisibility(View.GONE);
//                v.normal_half_video_parent.setVisibility(View.GONE);
//                v.relative_videoview_parent.setVisibility(View.VISIBLE);
//                v.video_view.setVisibility(View.VISIBLE);
//                v.slider.setVisibility(View.GONE);
//
//
//
//
//                v.video_view.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
//                    @Override
//                    public void onReady(@NonNull com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer youTubePlayer) {
//
//                        v.full_video_sound_icon.setVisibility(View.VISIBLE);
//
//
//
//                        String videoId = ""+models.get(position).getVideoUrl();
//                        youTubePlayer.loadVideo(videoId, 0);
//                   //     youTubePlayer.play();
//                        youTubePlayer.pause();
//
//
//                    }
//                });
//            }
//          }
//             if (models.get(position).getPostType().equals("normal_video")) {
//
//              v.half_video_downloader.releaseAllVideos();
//
//              if (models.get(position).getMediaType().equals("3")) {
//
//                v.half_video_downloader.releaseAllVideos();
//
//                v.relative_main.setVisibility(View.GONE);
//                v.imageView.setVisibility(View.GONE);
//                v.relative_videoview_parent.setVisibility(View.GONE);
//                v.relative_adds.setVisibility(View.GONE);
//                v.full_video_player_view.setVisibility(View.GONE);
//                v.relative_half_video_parent.setVisibility(View.GONE);
//                v.haff_video_view.setVisibility(View.GONE);
//                v.normal_half_video_parent.setVisibility(View.VISIBLE);
//                v.slider.setVisibility(View.GONE);
//
//
//              v.half_video_downloader.fullscreenButton.setClickable(false);
//
//              v.half_video_downloader.setUp(models.get(position).getVideoUrl(),""+models.get(position).getTitle());
//              Glide.with(context).load(R.drawable.beside_profile).into( v.half_video_downloader.thumbImageView);
//              v.half_video_downloader.onClick( v.half_video_downloader);
//              v.half_video_downloader.setClickable(true);
//
//
//            }
//              if (models.get(position).getMediaType().equals("4")) {
//
//              v.half_video_downloader.releaseAllVideos();
//
//
//              v.relative_main.setVisibility(View.GONE);
//              v.imageView.setVisibility(View.GONE);
//              v.relative_videoview_parent.setVisibility(View.GONE);
//              v.relative_adds.setVisibility(View.GONE);
//              v.full_video_player_view.setVisibility(View.GONE);
//              v.normal_half_video_parent.setVisibility(View.GONE);
//              v.relative_half_video_parent.setVisibility(View.VISIBLE);
//              v.haff_video_view.setVisibility(View.VISIBLE);
//
//
//              v.haff_video_view.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
//                  @Override
//                  public void onReady(@NonNull com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer youTubePlayer) {
//                      String videoId = ""+models.get(position).getVideoUrl();;
//                      youTubePlayer.loadVideo(videoId, 0);
//                      youTubePlayer.pause();
//                  }
//              });
//
//
//
//          }
//        }
//
//
//
//        v.full_video_sound_icon.setText(""+models.get(position).getTitle());
//        v.full_video_normal_username.setText(""+models.get(position).getTitle());
//
//        v.txtTitle.setText(models.get(position).getTitle());
//        v.txtDescription.setLetterSpacing(0.03f);
//        v.txtDescription.setText(models.get(position).getDescription());
//        v.txt_time.setText(commonMethods.getHrsFormatfromDate(models.get(position).getPublishedTime()));
//
//
//      */
///*  v.half_video_text_Title.setText(models.get(position).getTitle());
//        v.half_video_txt_time.setText(commonMethods.getHrsFormatfromDate(models.get(position).getPublishedTime()));
//        v.half_video_txtDescription.setText(models.get(position).getDescription());*//*
//
//
//
//        if(models.get(position).getCommentsFlag()){
//            v.comments_news.setVisibility(View.VISIBLE);
//        }else {
//            v.comments_news.setVisibility(View.GONE);
//        }
//*/
//
//
//       /* v.full_normal_vedio_sound_layout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                v.full_normal_vedio_sound_layout.setVisibility(View.GONE);
//                v.full_normal_vedio_sound_unmute_layout.setVisibility(View.VISIBLE);
//              audioManager.setStreamVolume( AudioManager.STREAM_MUSIC, 15, AudioManager.ADJUST_SAME);
//
//            }
//        });
//        v.full_normal_vedio_sound_unmute_layout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                v.full_normal_vedio_sound_layout.setVisibility(View.VISIBLE);
//                v.full_normal_vedio_sound_unmute_layout.setVisibility(View.GONE);
//
//                audioManager.setStreamVolume( AudioManager.STREAM_MUSIC, 0, AudioManager.ADJUST_SAME);
//
//                //  v.jzvdStd.setSoundEffectsEnabled(false);
//              */
//       /*  //To decrease media player volume
//                audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);*/
//       /*
//            }
//        });
//        v.normal_half_video_parent.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(click_event){
//
//                    Intent intent = new Intent("ADDRESS_BOOK_PAGE");
//                    intent.putExtra("message",click_event);//("message", click_event);
//                    click_event=false;
//                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
//
//
//
//                }else {
//                    Intent intent = new Intent("ADDRESS_BOOK_PAGE");
//                    intent.putExtra("message",click_event);//("message", click_event);
//                    click_event=true;
//                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
//
//                }
//            }
//        });
//        v.relative_videoview_parent.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//                if(click_event){
//
//                    Intent intent = new Intent("ADDRESS_BOOK_PAGE");
//                    intent.putExtra("message",click_event);//("message", click_event);
//                    click_event=false;
//                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
//
//
//
//                }else {
//                    Intent intent = new Intent("ADDRESS_BOOK_PAGE");
//                    intent.putExtra("message",click_event);//("message", click_event);
//                    click_event=true;
//                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
//
//                }
//            }
//        });
//        v.normal_half_video_download_image.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//
//
//
//                                String mInvitationUrl = models.get(position).getShareLink();
//                                String msg = models.get(position).getTitle()+" "
//                                        + mInvitationUrl;
//                                Intent intent = new Intent(Intent.ACTION_SEND);
//                                intent.setType("text/plain");
//                                intent.setPackage("com.whatsapp");
//                                intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl.toString());
//                                intent.putExtra(Intent.EXTRA_TEXT, msg);
//                                try{
//
//
//                                    List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(Intent.createChooser(intent,"Share Link" ), PackageManager.MATCH_DEFAULT_ONLY);
//                                    for (ResolveInfo resolveInfo : resInfoList) {
//                                        String packageName = resolveInfo.activityInfo.packageName;
//                                        //    context.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                    }
//                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                }
//                                catch (Exception e){
//                                    e.printStackTrace();
//                                }
//                                context.startActivity(Intent.createChooser(intent,"Share Link" ));
//
//
//            }
//        });
//        v.normal_half_video_share_linear.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//
//                                String mInvitationUrl = models.get(position).getShareLink();
//                                String msg = models.get(position).getTitle()+" "
//                                        + mInvitationUrl;
////                                Uri imageUri = Uri.parse(ImageUrl);
//                                Intent intent = new Intent(Intent.ACTION_SEND);
//                                intent.setType("text/plain");
//                                intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl.toString());
//                                intent.putExtra(Intent.EXTRA_TEXT, msg);
//                                try{
//
//
//                                    List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(Intent.createChooser(intent,"Share Link" ), PackageManager.MATCH_DEFAULT_ONLY);
//                                    for (ResolveInfo resolveInfo : resInfoList) {
//                                        String packageName = resolveInfo.activityInfo.packageName;
//                                        //    context.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                    }
//                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                }
//                                catch (Exception e){
//                                    e.printStackTrace();
//                                }
//                                //     intent.setType("image/*");
//                                context.startActivity(Intent.createChooser(intent,"Share Link" ));
//                                // ...
//
//
//            }
//        });
//        v.normal_haff_video_comments_news.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//                POST_ID=models.get(position).getPostId();
//
//                dialog12=new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
//                dialog12.setContentView(R.layout.activity_comments);
//                dialog12.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
//                dialog12.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                dialog12.setCanceledOnTouchOutside(false);
//
//                dialog12.show();
//
//
//                image_delete=dialog12.findViewById(R.id.image_delete);
//                post_news_button=dialog12.findViewById(R.id.post_news_button);
//                mRecyclerView = dialog12.findViewById(R.id.recyclerView);
//                write_comment=dialog12.findViewById(R.id.write_comment);
//                layoutLoading=dialog12.findViewById(R.id.layoutLoading);
//                total_number_ofcomments=dialog12.findViewById(R.id.total_number_ofcomments);
//                card_view=dialog12.findViewById(R.id.card_view);
//                linear_no_recyclerView=dialog12.findViewById(R.id.linear_no_recyclerView);
//                resize_layout=dialog12.findViewById(R.id.resize_layout);
//                LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(context,
//                        LinearLayoutManager.VERTICAL, false);
//                mRecyclerView.setLayoutManager(mLinearLayoutManager);
//                mRecyclerView.setVisibility(View.GONE);
//                layoutLoading.setVisibility(View.VISIBLE);
//
//
//
//                chooseLanguageModels=new ArrayList<>();
//
//                image_delete.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//
//                        dialog12.dismiss();
//                    }
//                });
//
//
//                write_comment.addTextChangedListener(new TextWatcher() {
//                    @Override
//                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//
//
//                    }
//
//                    @Override
//                    public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//
//                    }
//
//                    @Override
//                    public void afterTextChanged(Editable s) {
//
//                    }
//                });
//
//
//
//
//
//
//
//
//
//
//
//
//                post_news_button.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//                        if(write_comment.getText().toString().length()>=1){
//
//                            ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(write_comment.getWindowToken(), 0);
//
//                            WriteComments();
//
//                        }else {
//                            ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(write_comment.getWindowToken(), 0);
//
//                            Toast.makeText(context, "Write Your Comment ", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//
//                ShowAllComments();
//
//            }
//        });
//        v.normal_full_video_download.setOnClickListener(new View.OnClickListener() {
//             @Override
//             public void onClick(View view) {
//
//
//
//
//
//                     String ur=models.get(position).getVideoUrl();
//                     DownloadManager.Request request=new DownloadManager.Request(Uri.parse(ur));
//                     request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |DownloadManager.Request.NETWORK_MOBILE);
//                     request.setTitle("Download");
//                     request.setDescription("Downloading file..........!");
//
//                    request.allowScanningByMediaScanner();
//                     request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//                     request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,""+System.currentTimeMillis());
//
//                     DownloadManager manager=(DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
//                     manager.enqueue(request);
//
//
//             }
//         });*/
//
//
//        //  full normal video data for click event
//
//
//
//        // full image
//
//
//        // half normal imageview
//     /*   v.download_image.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//                try {
//
//                    v.image_download_visible.setVisibility(View.VISIBLE);
//                    v.image_download_gone.setVisibility(View.GONE);
//                    Bitmap bitmap = ScreenShott.getInstance().takeScreenShotOfView(v.layoutScreenShot);
//
//                    File direct = new File(Environment.getExternalStorageDirectory() + "/AnyNews");
//
//                    if (!direct.exists()) {
//                        File wallpaperDirectory = new File("/sdcard/AnyNews/");
//                        wallpaperDirectory.mkdirs();
//                    }
//
//                    File file = new File("/sdcard/AnyNews/", System.currentTimeMillis() + ".png");
//
//                    try {
//                        outputStream = new FileOutputStream(file);
//                        v.image_download_visible.setVisibility(View.GONE);
//                        v.image_download_gone.setVisibility(View.VISIBLE);
//                    } catch (FileNotFoundException e) {
//                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
//
//                    }
//
//                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
//
//                    try {
//                        outputStream.flush();
//                    } catch (IOException e) {
//
//                        showLog("MESAAGE "+e.getMessage());
//                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                    try {
//                        outputStream.close();
//
//                        countforshareanddownload("download",models.get(position).getPostId());
//                    } catch (IOException e) {
//                        showLog("MESAAGE "+e.getMessage());
//                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//
//
//
//                    Toast.makeText(context, "Image  Downloaded Successfully", Toast.LENGTH_SHORT).show();
//                }
//                catch (Exception e) {
//                    Toast.makeText(context, "Image is not loaded yet...", Toast.LENGTH_SHORT).show();
//                }
//
//            }
//        });
//        v.share_linear.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                v.comments_news.setVisibility(View.GONE);
//                v.download_image.setVisibility(View.GONE);
//                v.share_test.setVisibility(View.GONE);
//                v.share_image_view.setImageResource(R.drawable.final_video_data_two );
//
//
//                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                params.topMargin=5;
//                params.gravity = Gravity.RIGHT|Gravity.CENTER;
//                v.share_image_view.setLayoutParams(params);
//
//                if(!v.comments_news.isShown()&&!v.download_image.isShown()&&!v.share_test.isShown()){
//
//
//                    String mInvitationUrl =""+models.get(position).getShareLink();
//                    String msg = models.get(position).getTitle()+" "
//                            + mInvitationUrl;
////                                Uri imageUri = Uri.parse(ImageUrl);
//                    Intent intent = new Intent(Intent.ACTION_SEND);
//                    intent.setType("text/plain");
//                    intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl.toString());
//                    intent.putExtra(Intent.EXTRA_TEXT, msg);
//                    Bitmap b = ScreenShott.getInstance().takeScreenShotOfView(v.layoutScreenShot);
//                               *//* v.image_download_visible.setVisibility(View.GONE);
//                                v.image_download_gone.setVisibility(View.VISIBLE);*//*
//                    Bitmap bitmap = getImageFromBitmap(v.imageView);
//                    try{
//                        File file = new File(context.getExternalCacheDir(),"sample.png");
//                        FileOutputStream fOut = new FileOutputStream(file);
//                        b.compress(Bitmap.CompressFormat.PNG,100,fOut);
//                        fOut.flush();
//                        fOut.close();
//                        file.setReadable(true,true);
//                        Uri photoURI = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
//                        intent.putExtra(Intent.EXTRA_STREAM, photoURI);
//                        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(Intent.createChooser(intent,"Share Link" ), PackageManager.MATCH_DEFAULT_ONLY);
//                        for (ResolveInfo resolveInfo : resInfoList) {
//                            String packageName = resolveInfo.activityInfo.packageName;
//                            context.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                        }
//                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                    }
//                    catch (Exception e){
//                        e.printStackTrace();
//                    }
//
//                    v.comments_news.setVisibility(View.VISIBLE);
//                    v.download_image.setVisibility(View.VISIBLE);
//                    v.share_test.setVisibility(View.VISIBLE);
//
//
//
//                    if(v.download_image.isShown()&&v.share_test.isShown()){
//                        v.share_image_view.setImageResource(R.drawable.ic_share_black_24dp);
//                        intent.setType("image/*");
//                        context.startActivity(Intent.createChooser(intent,"Share Link" ));
//
//                    }else {
//
//                    }
//
//                }else {
//
//                }
//
//
//                // ...
//            }
//        });
//        v.comments_news.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                POST_ID=models.get(position).getPostId();
//
//                dialog12=new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
//                dialog12.setContentView(R.layout.activity_comments);
//                dialog12.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
//                dialog12.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                dialog12.setCanceledOnTouchOutside(false);
//
//                dialog12.show();
//
//
//                image_delete=dialog12.findViewById(R.id.image_delete);
//                post_news_button=dialog12.findViewById(R.id.post_news_button);
//                mRecyclerView = dialog12.findViewById(R.id.recyclerView);
//                write_comment=dialog12.findViewById(R.id.write_comment);
//                layoutLoading=dialog12.findViewById(R.id.layoutLoading);
//                total_number_ofcomments=dialog12.findViewById(R.id.total_number_ofcomments);
//                card_view=dialog12.findViewById(R.id.card_view);
//                linear_no_recyclerView=dialog12.findViewById(R.id.linear_no_recyclerView);
//                resize_layout=dialog12.findViewById(R.id.resize_layout);
//                LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(context,
//                        LinearLayoutManager.VERTICAL, false);
//                mRecyclerView.setLayoutManager(mLinearLayoutManager);
//                mRecyclerView.setVisibility(View.GONE);
//                layoutLoading.setVisibility(View.VISIBLE);
//
//
//
//                chooseLanguageModels=new ArrayList<>();
//
//                image_delete.setOnClickListener(new OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//
//                        dialog12.dismiss();
//                    }
//                });
//
//
//                write_comment.addTextChangedListener(new TextWatcher() {
//                    @Override
//                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//
//
//                    }
//
//                    @Override
//                    public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//
//                    }
//
//                    @Override
//                    public void afterTextChanged(Editable s) {
//
//                    }
//                });
//
//
//
//
//
//
//
//
//
//
//
//
//                post_news_button.setOnClickListener(new OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//                        if(write_comment.getText().toString().length()>=1){
//
//                            ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(write_comment.getWindowToken(), 0);
//
//                            WriteComments();
//
//                        }else {
//                            ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(write_comment.getWindowToken(), 0);
//
//                            Toast.makeText(context, "Write Your Comment ", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//
//                ShowAllComments();
//
//            }
//        });
//*/
//
//        // half vedio details
///*
//        v.half_video_download_image.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //
//                //
//
//
//                String appLink = "https://com.anyNews.anynews/";
//
//*//*
//                v.image_download_visible.setVisibility(View.VISIBLE);
//                v.image_download_gone.setVisibility(View.GONE);
//*/
//        /*
//                FirebaseDynamicLinks.getInstance().createDynamicLink()
//                        .setLink(Uri.parse(appLink+"?articleId="+SharedPrefsData.getString(context,Constants.ArticleNewsID,Constants.PREF_NAME)))
//                        .setDomainUriPrefix("https://anynews0.page.link")
//                        .setAndroidParameters(
//                                new DynamicLink.AndroidParameters.Builder("com.anyNews.anynews")
//                                        .setMinimumVersion(25)
//                                        .build())
//
//                        .buildShortDynamicLink()
//                        .addOnSuccessListener(new OnSuccessListener<ShortDynamicLink>() {
//                            @Override
//                            public void onSuccess(ShortDynamicLink shortDynamicLink) {
//
//                                Uri mInvitationUrl = shortDynamicLink.getShortLink();
//                                String msg = models.get(position).getTitle()+" "
//                                        + mInvitationUrl;
////                                Uri imageUri = Uri.parse(ImageUrl);
//                                Intent intent = new Intent(Intent.ACTION_SEND);
//                                intent.setType("text/plain");
//                                intent.setPackage("com.whatsapp");
//                                intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl.toString());
//                                intent.putExtra(Intent.EXTRA_TEXT, msg);
//                                try{
//
//
//                                    List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(Intent.createChooser(intent,"Share Link" ), PackageManager.MATCH_DEFAULT_ONLY);
//                                    for (ResolveInfo resolveInfo : resInfoList) {
//                                        String packageName = resolveInfo.activityInfo.packageName;
//                                        //    context.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                    }
//                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                }
//                                catch (Exception e){
//                                    e.printStackTrace();
//                                }
//                                //     intent.setType("image/*");
//                                context.startActivity(Intent.createChooser(intent,"Share Link" ));
//                                // ...
//                            }
//                        });
//
//
//
//
//            }
//        });
//        v.haff_video_comments_news.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//
//                POST_ID=models.get(position).getPostId();
//
//                dialog12=new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
//                dialog12.setContentView(R.layout.activity_comments);
//                dialog12.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
//                dialog12.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                dialog12.setCanceledOnTouchOutside(false);
//
//                dialog12.show();
//
//
//                image_delete=dialog12.findViewById(R.id.image_delete);
//                post_news_button=dialog12.findViewById(R.id.post_news_button);
//                mRecyclerView = dialog12.findViewById(R.id.recyclerView);
//                write_comment=dialog12.findViewById(R.id.write_comment);
//                layoutLoading=dialog12.findViewById(R.id.layoutLoading);
//                total_number_ofcomments=dialog12.findViewById(R.id.total_number_ofcomments);
//                card_view=dialog12.findViewById(R.id.card_view);
//                linear_no_recyclerView=dialog12.findViewById(R.id.linear_no_recyclerView);
//                resize_layout=dialog12.findViewById(R.id.resize_layout);
//                LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(context,
//                        LinearLayoutManager.VERTICAL, false);
//                mRecyclerView.setLayoutManager(mLinearLayoutManager);
//                mRecyclerView.setVisibility(View.GONE);
//                layoutLoading.setVisibility(View.VISIBLE);
//
//
//
//                chooseLanguageModels=new ArrayList<>();
//
//                image_delete.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//
//                        dialog12.dismiss();
//                    }
//                });
//
//
//                write_comment.addTextChangedListener(new TextWatcher() {
//                    @Override
//                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//
//
//                    }
//
//                    @Override
//                    public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//
//                    }
//
//                    @Override
//                    public void afterTextChanged(Editable s) {
//
//                    }
//                });
//
//
//
//
//
//
//
//
//
//
//
//
//                post_news_button.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//                        if(write_comment.getText().toString().length()>=1){
//
//                            ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(write_comment.getWindowToken(), 0);
//
//                            WriteComments();
//
//                        }else {
//                            ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(write_comment.getWindowToken(), 0);
//
//                            Toast.makeText(context, "Write Your Comment ", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//
//                ShowAllComments();
//
//
//            }
//        });
//        v.half_video_share_linear.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//                String appLink = "https://com.anyNews.anynews/";
//
//*//*
//                v.image_download_visible.setVisibility(View.VISIBLE);
//                v.image_download_gone.setVisibility(View.GONE);
//*//*
//                FirebaseDynamicLinks.getInstance().createDynamicLink()
//                        .setLink(Uri.parse(appLink+"?articleId="+SharedPrefsData.getString(context,Constants.ArticleNewsID,Constants.PREF_NAME)))
//                        .setDomainUriPrefix("https://anynews0.page.link")
//                        .setAndroidParameters(
//                                new DynamicLink.AndroidParameters.Builder("com.anyNews.anynews")
//                                        .setMinimumVersion(25)
//                                        .build())
//
//                        .buildShortDynamicLink()
//                        .addOnSuccessListener(new OnSuccessListener<ShortDynamicLink>() {
//                            @Override
//                            public void onSuccess(ShortDynamicLink shortDynamicLink) {
//
//                                Uri mInvitationUrl = shortDynamicLink.getShortLink();
//                                String msg = models.get(position).getTitle()+" "
//                                        + mInvitationUrl;
////                                Uri imageUri = Uri.parse(ImageUrl);
//                                Intent intent = new Intent(Intent.ACTION_SEND);
//                                intent.setType("text/plain");
//                                intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl.toString());
//                                intent.putExtra(Intent.EXTRA_TEXT, msg);
//                                try{
//
//
//                                    List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(Intent.createChooser(intent,"Share Link" ), PackageManager.MATCH_DEFAULT_ONLY);
//                                    for (ResolveInfo resolveInfo : resInfoList) {
//                                        String packageName = resolveInfo.activityInfo.packageName;
//                                    //    context.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                    }
//                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                }
//                                catch (Exception e){
//                                    e.printStackTrace();
//                                }
//                           //     intent.setType("image/*");
//                                context.startActivity(Intent.createChooser(intent,"Share Link" ));
//                                // ...
//                            }
//                        });
//
//
//
//
//
//            }
//        });*/
//
//        // full video
//       /* v.full_video_share.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//
//                String appLink = "https://com.anyNews.anynews/";
//
//                FirebaseDynamicLinks.getInstance().createDynamicLink()
//                        .setLink(Uri.parse(appLink+"?articleId="+SharedPrefsData.getString(context,Constants.ArticleNewsID,Constants.PREF_NAME)))
//                        .setDomainUriPrefix("https://anynews0.page.link")
//                        .setAndroidParameters(
//                                new DynamicLink.AndroidParameters.Builder("com.anyNews.anynews")
//                                        .setMinimumVersion(25)
//                                        .build())
//
//                        .buildShortDynamicLink()
//                        .addOnSuccessListener(new OnSuccessListener<ShortDynamicLink>() {
//                            @Override
//                            public void onSuccess(ShortDynamicLink shortDynamicLink) {
//
//                                Uri mInvitationUrl = shortDynamicLink.getShortLink();
//                                String msg = "AnyNews "+" "
//                                        + mInvitationUrl;
////                                Uri imageUri = Uri.parse(ImageUrl);
//                                Intent intent = new Intent(Intent.ACTION_SEND);
//                                intent.setType("text/plain");
//                                intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl.toString());
//                                intent.putExtra(Intent.EXTRA_TEXT, msg);
//                                try{
//
//
//                                    List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(Intent.createChooser(intent,"Share Link" ), PackageManager.MATCH_DEFAULT_ONLY);
//                                    for (ResolveInfo resolveInfo : resInfoList) {
//                                        String packageName = resolveInfo.activityInfo.packageName;
//                                        //    context.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                    }
//                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                }
//                                catch (Exception e){
//                                    e.printStackTrace();
//                                }
//                                //     intent.setType("image/*");
//                                context.startActivity(Intent.createChooser(intent,"Share Link" ));
//                                // ...
//                            }
//                        });
//
//
//
//
//            }
//        });
//        v.full_video_download.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                POST_ID=models.get(position).getPostId();
//
//                dialog12=new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
//                dialog12.setContentView(R.layout.activity_comments);
//                dialog12.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
//                dialog12.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                dialog12.setCanceledOnTouchOutside(false);
//
//                dialog12.show();
//
//
//                image_delete=dialog12.findViewById(R.id.image_delete);
//                post_news_button=dialog12.findViewById(R.id.post_news_button);
//                mRecyclerView = dialog12.findViewById(R.id.recyclerView);
//                write_comment=dialog12.findViewById(R.id.write_comment);
//                layoutLoading=dialog12.findViewById(R.id.layoutLoading);
//                total_number_ofcomments=dialog12.findViewById(R.id.total_number_ofcomments);
//                card_view=dialog12.findViewById(R.id.card_view);
//                linear_no_recyclerView=dialog12.findViewById(R.id.linear_no_recyclerView);
//                resize_layout=dialog12.findViewById(R.id.resize_layout);
//                LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(context,
//                        LinearLayoutManager.VERTICAL, false);
//                mRecyclerView.setLayoutManager(mLinearLayoutManager);
//                mRecyclerView.setVisibility(View.GONE);
//                layoutLoading.setVisibility(View.VISIBLE);
//
//
//
//                chooseLanguageModels=new ArrayList<>();
//
//                image_delete.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//
//                        dialog12.dismiss();
//                    }
//                });
//
//
//                write_comment.addTextChangedListener(new TextWatcher() {
//                    @Override
//                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//
//
//                    }
//
//                    @Override
//                    public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//
//                    }
//
//                    @Override
//                    public void afterTextChanged(Editable s) {
//
//                    }
//                });
//
//
//                post_news_button.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//                        if(write_comment.getText().toString().length()>=1){
//
//                            ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(write_comment.getWindowToken(), 0);
//
//                            WriteComments();
//
//                        }else {
//                            ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(write_comment.getWindowToken(), 0);
//
//                            Toast.makeText(context, "Write Your Comment ", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//
//                ShowAllComments();
//
//            }
//        });
//        v.full_video_whatsup.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//                                String mInvitationUrl = models.get(position).getShareLink();
//                                String msg = " "+models.get(position).getTitle()
//                                        + mInvitationUrl;
////                                Uri imageUri = Uri.parse(ImageUrl);
//                                Intent intent = new Intent(Intent.ACTION_SEND);
//                                intent.setType("text/plain");
//                                intent.setPackage("com.whatsapp");
//
//                                intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl.toString());
//                                intent.putExtra(Intent.EXTRA_TEXT, msg);
//                                try{
//
//
//                                    List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(Intent.createChooser(intent,"Share Link" ), PackageManager.MATCH_DEFAULT_ONLY);
//                                    for (ResolveInfo resolveInfo : resInfoList) {
//                                        String packageName = resolveInfo.activityInfo.packageName;
//                                        //    context.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                    }
//                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                }
//                                catch (Exception e){
//                                    e.printStackTrace();
//                                }
//                                //     intent.setType("image/*");
//                                context.startActivity(Intent.createChooser(intent,"Share Link" ));
//
//            }
//        });*/
//
//
//      /*  v.relative_adds.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Intent intent = new Intent("ADDRESS_BOOK_PAGE");
//                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
//            }
//        });
//        v.relative_half_video_parent.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(click_event){
//
//                    Intent intent = new Intent("ADDRESS_BOOK_PAGE");
//                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
//
//
//
//                }else {
//                    Intent intent = new Intent("ADDRESS_BOOK_PAGE");
//                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
//
//                }
//            }
//        });
//        v.video_view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(click_event){
//
//                    Intent intent = new Intent("ADDRESS_BOOK_PAGE");
//                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
//
//
//
//                }else {
//                    Intent intent = new Intent("ADDRESS_BOOK_PAGE");
//                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
//
//                }
//            }
//        });
//*/
//
//
//
//    }
//
//
//    @Override
//    public void onViewRecycled(@NonNull ViewHolder holder) {
////        holder.text.setText("Unbound");
//    }
//
//
//
//    private void   countforshareanddownload(String share,int post_id){
//
//             JsonObject jsonObject = new JsonObject();
//             jsonObject.addProperty("draft_id", SharedPrefsData.getInt(context,Constants.DraftID,Constants.PREF_NAME));
//             jsonObject.addProperty("post_id", post_id);
//             jsonObject.addProperty("property", share);
//
//
//
//            /* skip  0 --> 5
//              skip  1-->15 */
//             APIService apiService = RestApiClient.getClient().create(APIService.class);
//             Call<EncreptionRequest> apiResponse = apiService.update_post_count( SharedPrefsData.getString(context, Constants.Version_ID, Constants.PREF_NAME),jsonObject);
//             apiResponse.enqueue(new Callback<EncreptionRequest>() {
//                 @Override
//
//                 public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
//
//                   /*  LikeDownloadRespouse likeDownloadRespouse = response.body();
//
//                     if (likeDownloadRespouse.getSuccess()) {
//
//
//                     }*/
//
//                 }
//
//
//                 @Override
//                 public void onFailure(Call<EncreptionRequest> call, Throwable t) {
//
//                     Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
//
//                 }
//             });
//
//         }
//    private JsonObject TakeData() {
//
//        ListOfCommentsRequest requestModel = new ListOfCommentsRequest();
//
///*
//        requestModel.setDraftId(SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME));
//        requestModel.setPostId(POST_ID);
//        requestModel.setComment(""+write_comment.getText().toString());*/
//
//
//        return new Gson().toJsonTree(requestModel).getAsJsonObject();
//    }
//    private JsonObject allcomments() {
//
//        ListOfCommentsRequest requestModel = new ListOfCommentsRequest();
//
///*
//        requestModel.setDraftId(SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME));
//        requestModel.setPostId(POST_ID);
//        requestModel.setLimit(30);
//        requestModel.setSkip(0);*/
//
//        return new Gson().toJsonTree(requestModel).getAsJsonObject();
//    }
//    private void WriteComments() {
//
//
//        JsonObject otp = TakeData();
//        showLog("API REQ   "+otp.toString());
//        APIService apiService = RestApiClient.getClient().create(APIService.class);
//
//        Call<EncreptionRequest> otp_generation = apiService.comment(SharedPrefsData.getString(context, Constants.Version_ID, Constants.PREF_NAME),otp);
//        try {
//            otp_generation.enqueue(new Callback<EncreptionRequest>() {
//
//                @Override
//                public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
//
//
//                    EncreptionRequest encreptionRequest=response.body();
//                    showLog("API RES   "+response.body().getData());
//                    String encrpt_data = null;
//                    /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                        encrpt_data = decrypt(encreptionRequest.getData());
//                    }*/
//                    encrpt_data = decrypt(encreptionRequest.getData());
//
//                    showLog("ENCRYPT"+encrpt_data);
//
//                    Gson gsonObj = new Gson();
//                    CommentResponuse validateUserOtp = gsonObj.fromJson(encrpt_data, CommentResponuse.class);
//
//
//                        if (validateUserOtp.getSuccess()){
//
//                            write_comment.setText("");
//                            ShowAllComments();
//
//
//                        }  else {
//
//
//                        }
//
//                }
//
//                @Override
//                public void onFailure(Call<EncreptionRequest> call, Throwable t) {
//
//
//                    Toast.makeText(context,t.getMessage(),Toast.LENGTH_LONG).show();
//
//
//                }
//            });
//
//        } catch (Exception e){
//            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }
//    private void ShowAllComments() {
//
//       /* JsonObject otp = allcomments();
//
//        APIService apiService = RestApiClient.getClient().create(APIService.class);
//
//        Call<ListOfCommentsResponse> otp_generation = apiService.listofComments(SharedPrefsData.getString(context, Constants.Version_ID, Constants.PREF_NAME),otp);
//        try {
//            otp_generation.enqueue(new Callback<ListOfCommentsResponse>() {
//
//                @Override
//                public void onResponse(Call<ListOfCommentsResponse> call, Response<ListOfCommentsResponse> response) {
//
//                    if(response.isSuccessful()) {
//
//                        ListOfCommentsResponse validateUserOtp = response.body();
//
//                        chooseLanguageModels.clear();
//
//                        if (validateUserOtp.getSuccess()){
//
//
//                            *//*linear_no_recyclerView*//*
//                            if(validateUserOtp.getExtras().getData().size()==0){
//                                layoutLoading.setVisibility(View.GONE);
//                                mRecyclerView.setVisibility(View.GONE);
//                                linear_no_recyclerView.setVisibility(View.VISIBLE);
//
//                            }else {
//                                for(int i=0;i<validateUserOtp.getExtras().getData().size();i++){
//
//                                    ListOfCommentsResponse.Datum datum=new ListOfCommentsResponse.Datum();
//
//                                    datum.setId(validateUserOtp.getExtras().getData().get(i).getId());
//                                    datum.setPostId(validateUserOtp.getExtras().getData().get(i).getPostId());
//                                    datum.setDraftId(validateUserOtp.getExtras().getData().get(i).getDraftId());
//                                    datum.setUserName(validateUserOtp.getExtras().getData().get(i).getUserName());
//                                    datum.setComment(validateUserOtp.getExtras().getData().get(i).getComment());
//                                    datum.setCreatedAt(validateUserOtp.getExtras().getData().get(i).getCreatedAt());
//                                    datum.setCommentTime(validateUserOtp.getExtras().getData().get(i).getCommentTime());
//                                    chooseLanguageModels.add(datum);
//                                }
//                                linear_no_recyclerView.setVisibility(View.GONE);
//                                mRecyclerView.setVisibility(View.VISIBLE);
//                                layoutLoading.setVisibility(View.GONE);
//                               *//* MailAdapter mMailAdapter = new MailAdapter(context, chooseLanguageModels);
//                                mRecyclerView.setAdapter(mMailAdapter);*//*
//                                total_number_ofcomments.setText("Comments( "+chooseLanguageModels.size()+" )");
//
//                            }
//
//
//                     *//*       MailAdapter mMailAdapter = new MailAdapter(this, mEmailData);
//                            mRecyclerView.setAdapter(mMailAdapter);
//
//                            categoryAdapter = new CategoryAdapter(getApplicationContext(),validateUserOtp);
//                            recycler_comments.setLayoutManager(new LinearLayoutManager(getApplicationContext(),RecyclerView.VERTICAL,true));
//                            recycler_comments.setAdapter(categoryAdapter);*//*
////                            recycler_comments.smoothScrollToPosition(validateUserOtp.getExtras().getData().size()-1);
//
//
//                        }  else {
//
//                            //     Toast.makeText(getApplicationContext(),validateUserOtp.getExtras().getMsg() , Toast.LENGTH_SHORT).show();
//
//                        }
//
//                    }
//
//                }
//
//                @Override
//                public void onFailure(Call<ListOfCommentsResponse> call, Throwable t) {
//
//
//                    Toast.makeText(context,t.getMessage(),Toast.LENGTH_LONG).show();
//
//
//                }
//            });
//
//        } catch (Exception e){
//            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
//        }*/
//    }
//
//    private Bitmap getImageFromBitmap(View view){
//        Bitmap bitmap =Bitmap.createBitmap(view.getWidth(),view.getHeight(),Bitmap.Config.ARGB_8888);
//        Canvas camvas = new Canvas(bitmap);
//        Drawable drawable = view.getBackground();
//        if (drawable!=null){
//            drawable.draw(camvas);
//        }
//        else{
//            camvas.drawColor(Color.WHITE);
//        }
//        view.draw(camvas);
//        return bitmap;
//    }
//    @Override
//    public int getItemCount() {
//        return models.size();
//    }
//
//}