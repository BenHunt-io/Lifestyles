package theappfoundry.lifestyles;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class DrawingPractice2 extends Activity {
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set a Rect for the 200 x 200 px center of a 400 x 400 px area
        Rect rect = new Rect();
        rect.set(100, 100, 700, 700);

        //Allocate a new Bitmap at 400 x 400 px
        Bitmap bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        //Make a new view and lay it out at the desired Rect dimensions
        TextView view = new TextView(this);
        view.setText("This is a custom drawn textview");
        view.setBackgroundColor(Color.RED);
        view.setGravity(Gravity.CENTER);

        //Measure the view at the exact dimensions (otherwise the text won't center correctly)
        int widthSpec = View.MeasureSpec.makeMeasureSpec(rect.width(), View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(rect.height(), View.MeasureSpec.EXACTLY);
        view.measure(widthSpec, heightSpec);

        //Lay the view out at the rect width and height
        view.layout(0, 0, rect.width(), rect.height());

        //Translate the Canvas into position and draw it
        canvas.save();
        canvas.translate(rect.left, rect.top);
        view.draw(canvas);
        canvas.restore();

//        //To make sure it works, set the bitmap to an ImageView
//        ImageView imageView = new ImageView(this);
//        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        setContentView(imageView);
//        imageView.setScaleType(ImageView.ScaleType.CENTER);
//        imageView.setImageBitmap(bitmap);
    }
}





















//public class DrawingPractice2 extends AppCompatActivity {
//
//    private String TAG = "I'm GAY";
//    private Bitmap b;
//    private Canvas c;
//    private CustomDrawableView mCustomDrawableView;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_drawing_practice2);
//
//
//        //Set a Rect for the 200 x 200 px center of a 400 x 400 px area
//        Rect rect = new Rect();
//        rect.set(100, 100, 300, 300);
//
//        setUpCanvas();
//        Paint p = new Paint();
//        //c.drawRect(25, 25, 100, 100, p);
//        mCustomDrawableView = new CustomDrawableView(this);
//        mCustomDrawableView.draw();
//        mCustomDrawableView.layout(0,0,1000,1000);
//        c.save();
//        c.translate(rect.left, rect.top);
//        mCustomDrawableView.draw(c);
//        c.restore();
//
//    }
//
//
//    public class CustomDrawableView extends View {
//
//        public ShapeDrawable mDrawable;
//        public float x = 25;
//        public float y = 25;
//        public float width = 500;
//        public float height = 500;
//
//
//        public CustomDrawableView(Context context) {
//            super(context);
//            mDrawable = new ShapeDrawable(new RectShape());
//        }
//
//        public void draw() {
//
//
//
////            this.width = event.getX();
////            this.height = event.getY();
////            Log.d(TAG, "Width: " + this.width);
////            Log.d(TAG, "height: " + this.height);
////            Log.d(TAG, "x: " + this.x);
////            Log.d(TAG, "y: " + this.y);
//
//
//            mDrawable.getPaint().setColor(0xff74AC23);
//            mDrawable.setBounds(Math.round(x), Math.round(y), Math.round(width), Math.round(height - y));
//           // mDrawable.draw(c);
//
//
//        }
//
//        protected void onDraw(Canvas canvas) {
//
//        }
//    }
//
//
//
//
//
//    public void setUpCanvas() {
//
//        /**
//         * Getting our LinearLayout which has our map in it. (For finding width/height of canvas)
//         */
//        //LinearLayout mapCanvas = (LinearLayout)findViewById(R.id.mapLayout);
//
//        /**
//         * Create bitmap which the drawing will be in., wid, height, Config.
//         * ARGB_8888 is each pixel is stored on 8 bytes. Basically changes how its stored
//         */
//        //Log.d(TAG, "setUpBitmap: " + mapCanvas.getWidth());
//        b = Bitmap.createBitmap(500, 500,
//                Bitmap.Config.ARGB_8888);
//
//        /**
//         * A Canvas works for you as a pretense, or interface, to the actual surface upon which
//         * your graphics will be drawn — it holds all of your "draw" call
//         */
//        c = new Canvas(b); // Have to draw through the canvas using the underlying bitmap
//
//
//    }
//}


