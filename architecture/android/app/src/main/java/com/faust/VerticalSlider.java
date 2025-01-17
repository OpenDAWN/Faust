package com.faust;

import com.dsp_faust.dsp_faust;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.VerticalSeekBar;

/*
 * TODO: with accelerometers, when using setNormalizedValue the handle of the 
 * slider doesn't move. I think the only way to solve this problem might be
 * to have our own implementation of vertical slider which obviously would be
 * a lot of work...
 */

/*
 * Create a horizontal slider that displays its current value on its left. 
 */

class VerticalSlider {
	float min = 0.0f, max = 100.0f, step = 1.0f;
	int id = 0;
	String decimalsDisplay = "", address = "";
	LinearLayout frame, sliderLayout, localVerticalGroup;
	VerticalSeekBar slider;
	TextView textValue, textLabel;
	Point size;
	
	/*
	 * The constructor.
	 * addr: the tree address of the parameter controlled by the slider
	 * currentParameterID: the current parameter id in the parameters tree
	 * width: width of the view in pxs
	 * backgroundColor: grey level of the background of the view (0-255)
	 */
	public VerticalSlider(Context c, String addr, int currentParameterID,
			int width, int backgroundColor, boolean visibility){
		WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		size = new Point();
		display.getSize(size);
		
		id = currentParameterID;
		address = addr;
		
		int sliderHeight = 300*(size.x+size.y)/2080;
		slider = new VerticalSeekBar(c);
		slider.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, sliderHeight));
		
		frame = new LinearLayout(c);
		frame.setLayoutParams(new ViewGroup.LayoutParams(
				width, ViewGroup.LayoutParams.WRAP_CONTENT));
		frame.setOrientation(LinearLayout.VERTICAL);
		frame.setBackgroundColor(Color.rgb(backgroundColor, 
				backgroundColor, backgroundColor));
		frame.setPadding(2,2,2,2);
		
		sliderLayout = new LinearLayout(c);
		sliderLayout.setOrientation(LinearLayout.VERTICAL);
		sliderLayout.setGravity(Gravity.CENTER);
		
		localVerticalGroup = new LinearLayout(c);
		localVerticalGroup.setOrientation(LinearLayout.VERTICAL);
		localVerticalGroup.setGravity(Gravity.CENTER);
		localVerticalGroup.setBackgroundColor(Color.rgb(backgroundColor+15, 
				backgroundColor+15, backgroundColor+15));
		
		textLabel = new TextView(c);
		textLabel.setGravity(Gravity.CENTER);
		
		textValue = new TextView(c);
		textValue.setGravity(Gravity.CENTER);
		if(visibility){
			sliderLayout.addView(textValue);
			sliderLayout.addView(slider);
			localVerticalGroup.addView(textLabel);
			localVerticalGroup.addView(sliderLayout);
			frame.addView(localVerticalGroup);
		}
	}
	
	/*
	 * Set the slider parameters
	 * label: the name of the parameter
	 * minimum: the slider's minimum value
	 * maximum: the slider's maximum value
	 * stp: the slider's step
	 */
	public void setParams(String label, float minimum, float maximum, float stp){
		textLabel.setText(label);
		min = minimum;
		max = maximum;
		step = stp;
		slider.setMax(Math.round((max-min)*(1/step)));
		int decimals = 0;
		if(step>=1) decimals = 1;
		else if(step<1 && step>=0.1) decimals = 1;
		else decimals = 2;
		decimalsDisplay = "%."+decimals+"f";
	}
	
	/*
	 * Set the value displayed next to the slider
	 */
	public void setDisplayedValue(float theValue){
		textValue.setText(String.format(decimalsDisplay, theValue));
	}
	
	/*
	 * Set the slider's value
	 */
	// TODO: this screwed but was fixed to work with the multi interface 
	// but there might still be weird things going on...
	public void setValue(float theValue){
		if(theValue<=0 && min<0) slider.setProgress(Math.round(theValue*(1/step)+min));
		else slider.setProgress(Math.round(theValue*(1/step)-min));
		setDisplayedValue(theValue);
	}
	
	/*
	 * Set the value of the slider as a number between 0 and 1
	 */
	public void setNormizedValue(float theValue){
		slider.setProgress(Math.round(theValue*(max-min)/step));
	}
	
	/*
	 * Add the slider to group
	 */
	public void addTo(LinearLayout group){
		group.addView(frame);
	}
	
	/*
	 * Set the slider's listeners
	 */
	public void linkTo(final ParametersInfo parametersInfo, final ConfigWindow parametersWindow, final HorizontalScrollView horizontalScroll){
		localVerticalGroup.setOnLongClickListener(new OnLongClickListener(){
			public boolean onLongClick (View v){
				if(!parametersInfo.locked) parametersWindow.showWindow(parametersInfo, id);
				return true;
			}
		});
		
		slider.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
				parametersInfo.accelItemFocus[id] = 0;
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
				parametersInfo.accelItemFocus[id] = 1;
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				parametersInfo.values[id] = (float) progress*step + min;
				dsp_faust.setParam(address, parametersInfo.values[id]);
				setDisplayedValue(parametersInfo.values[id]);
	          }
	    });
	    
	    slider.setOnTouchListener(new OnTouchListener()
	    {
	        public boolean onTouch(final View view, final MotionEvent event)
	        {
	          if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE)
	            horizontalScroll.requestDisallowInterceptTouchEvent(true);
	          return false;
	        }
	    });
	}
}
