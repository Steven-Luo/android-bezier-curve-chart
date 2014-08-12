package com.cn.naive.sample;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.cn.naive.beziercurvechart.R;
import com.cn.naive.lib.view.BezierCurveChart;
import com.cn.naive.lib.view.BezierCurveChart.Point;

public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			
			List<BezierCurveChart.Point> points=new ArrayList<BezierCurveChart.Point>();
			for(int i=0;i<10;i++) {
				points.add(new Point(i, (float) (Math.random()*10)));
			}
			
			BezierCurveChart bezierCurveChart = 
					(BezierCurveChart) rootView.findViewById(R.id.bezier_curve_chart);
			String tipText = "3 hours/day on average";
			bezierCurveChart.init(points, 
					new String[] { "0:00", "6:00", "12:00", "18:00", "24:00" }, tipText);

			return rootView;
		}
	}

}
