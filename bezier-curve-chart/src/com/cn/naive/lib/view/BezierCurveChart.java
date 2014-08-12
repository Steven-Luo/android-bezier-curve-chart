package com.cn.naive.lib.view;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class BezierCurveChart extends View {
	public static class Point {
		public static final Comparator<Point> X_COMPARATOR = new Comparator<Point>() {
			@Override
			public int compare(Point lhs, Point rhs) {
				return (int) (lhs.x * 1000 - rhs.x * 1000);
			}
		};
		
		public float x;
		public float y;

		public Point(float x, float y) {
			this.x = x;
			this.y = y;
		}

		public Point() {
		}

		@Override
		public String toString() {
			return "(" + x + ", " + y + ")";
		}
	}
	
	private static final float CURVE_LINE_WIDTH = 4f;
	private static final float HALF_TIP_HEIGHT = 16;
	
	private static final String TAG = BezierCurveChart.class.getSimpleName();
	
	private Point[] adjustedPoints;
	private Paint borderPaint = new Paint();
	private Paint chartBgPaint = new Paint();
	// The rect of chart, x labels on the bottom are not included
	private Rect chartRect = new Rect();
	private Paint curvePaint = new Paint();
	private Path curvePath = new Path();
	private Paint fillPaint = new Paint();
	private Path fillPath = new Path();

	private Paint gridPaint = new Paint();
	private Paint labelPaint = new Paint();
	private String[] labels;
	private float maxY;

	private List<Point> originalList;
	private float scaleY;
	private Rect textBounds = new Rect();
	private Paint tipLinePaint = new Paint();
	private Paint tipPaint = new Paint();
	private Rect tipRect = new Rect();
	private RectF tipRectF = new RectF();
	private String tipText;
	private Paint tipTextPaint = new Paint();

	{
		borderPaint.setColor(Color.WHITE);
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setStrokeCap(Paint.Cap.SQUARE);
		borderPaint.setStrokeWidth(4.0f);
		borderPaint.setAntiAlias(true);

		curvePaint.setStyle(Paint.Style.STROKE);
		curvePaint.setStrokeCap(Paint.Cap.ROUND);
		curvePaint.setStrokeWidth(CURVE_LINE_WIDTH);
		curvePaint.setColor(Color.rgb(0x00, 0x89, 0xd8));
		curvePaint.setAntiAlias(true);
		curvePaint.setAlpha(200);

		fillPaint.setStyle(Paint.Style.FILL);
		fillPaint.setColor(Color.rgb(0x00, 0xd2, 0xff));
		fillPaint.setAlpha(170);
		fillPaint.setAntiAlias(true);

		chartBgPaint.setStyle(Paint.Style.FILL);
		chartBgPaint.setColor(Color.argb(0x88, 0xDD, 0xDD, 0xDD));
		chartBgPaint.setAlpha(180);
		chartBgPaint.setAntiAlias(true);

		gridPaint.setStyle(Paint.Style.STROKE);
		gridPaint.setStrokeCap(Paint.Cap.SQUARE);
		gridPaint.setColor(Color.argb(0x92, 0xD0, 0xD0, 0xD0));
		gridPaint.setAntiAlias(true);
		gridPaint.setStrokeWidth(3.0f);

		tipLinePaint.setStyle(Paint.Style.STROKE);
		tipLinePaint.setStrokeCap(Paint.Cap.SQUARE);
		tipLinePaint.setStrokeWidth(1.5f);
		tipLinePaint.setColor(Color.rgb(0x00, 0x89, 0xd8));
		tipLinePaint.setAntiAlias(true);
		tipLinePaint.setAlpha(220);

		tipPaint.setStyle(Paint.Style.FILL);
		tipPaint.setColor(Color.rgb(0x00, 0x89, 0xd8));
		tipPaint.setAntiAlias(true);

		tipTextPaint.setColor(Color.WHITE);
		tipTextPaint.setTextSize(21f);
		tipTextPaint.setAntiAlias(true);

		labelPaint.setColor(Color.rgb(0x71, 0x71, 0x71));
		labelPaint.setTextSize(21f);
		labelPaint.setAntiAlias(true);
	}

	public BezierCurveChart(Context context) {
		super(context);
	}

	public BezierCurveChart(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private void adjustPoints(int chartWidth, int chartHeight) {
		maxY = 0;
		// find max y coodinate
		for (Point p : originalList) {
			if (p.y > maxY) {
				maxY = p.y;
			}
		}

		//Y coodinate sacle
		scaleY = chartHeight / maxY;

		float axesSpan = originalList.get(originalList.size() - 1).x - originalList.get(0).x;
		float startX = originalList.get(0).x;

		for (int i = 0; i < originalList.size(); i++) {
			Point p = originalList.get(i);

			Point newPoint = new Point();
			newPoint.x = (p.x - startX) * chartWidth / axesSpan + chartRect.left;

			newPoint.y = p.y * scaleY;
			newPoint.y = chartHeight - newPoint.y;

			adjustedPoints[i] = newPoint;
		}
	}
	
	private void buildPath(Path path) {
		//Important!
		path.reset();

		path.moveTo(adjustedPoints[0].x, adjustedPoints[0].y);
		int pointSize = adjustedPoints.length;

		for (int i = 0; i < adjustedPoints.length - 1; i++) {
			float pointX = (adjustedPoints[i].x + adjustedPoints[i + 1].x) / 2;
			float pointY = (adjustedPoints[i].y + adjustedPoints[i + 1].y) / 2;

			float controlX = adjustedPoints[i].x;
			float controlY = adjustedPoints[i].y;

			path.quadTo(controlX, controlY, pointX, pointY);
		}
		path.quadTo(adjustedPoints[pointSize - 1].x, adjustedPoints[pointSize - 1].y, adjustedPoints[pointSize - 1].x,
				adjustedPoints[pointSize - 1].y);
	}

	private void drawCurve(Canvas canvas, float width, float height) {
		buildPath(curvePath);
		buildPath(fillPath);

		fillPath.lineTo(chartRect.right, chartRect.bottom);
		fillPath.lineTo(chartRect.left, chartRect.bottom);
		fillPath.lineTo(chartRect.left, adjustedPoints[0].y);
		fillPath.close();

		canvas.drawPath(fillPath, fillPaint);
		canvas.drawPath(curvePath, curvePaint);
	}

	private void drawGrid(Canvas canvas, int width) {

		canvas.drawRect(chartRect, chartBgPaint);

		int gridCount = labels.length - 1;
		float part = (float) width / gridCount;

		for (int i = 1; i < gridCount; i++) {
			float x = chartRect.left + part * i;
			canvas.drawLine(x, chartRect.top, x, chartRect.bottom, gridPaint);
		}
	}

	/**
	 * Draw labels on the bottom
	 * @param canvas
	 */
	private void drawLabels(Canvas canvas) {

		int width = chartRect.right - chartRect.left;

		float labelY = chartRect.bottom;
		float part = (float) width / (labels.length - 1);

		for (int i = 0; i < labels.length; i++) {
			String s = labels[i];
			float centerX = chartRect.left + part * i;
			float labelWidth = getTextWidth(labelPaint, s);
			float labelX;
			if (i == 0) {
				labelX = chartRect.left;
			} else if (i == labels.length - 1) {
				labelX = chartRect.right - labelWidth;
			} else {
				labelX = centerX - labelWidth / 2;
			}
			canvas.drawText(s, labelX, labelY, labelPaint);
		}

		chartRect.bottom = (int) (chartRect.bottom - getTextHeight(labelPaint) * 1.2);
	}

	/**
	 * Draw tip on the middle of the chart
	 * @param canvas
	 * @param width
	 * @param height
	 */
	private void drawTip(Canvas canvas, float width, float height) {

		float totalHeight = 0;
		for (Point p : adjustedPoints) {
			totalHeight += p.y;
		}

		float tipLineY = totalHeight / adjustedPoints.length;

		if (tipLineY + HALF_TIP_HEIGHT >= chartRect.bottom) {
			tipLineY = chartRect.bottom - HALF_TIP_HEIGHT - 4;
		}

		String text = tipText;
		tipTextPaint.getTextBounds(text, 0, 1, textBounds);

		float centerX = chartRect.left + width / 2;
		float textWidth = getTextWidth(tipTextPaint, text);

		tipRect.left = (int) (centerX - textWidth / 2 - 23);
		tipRect.right = (int) (centerX + textWidth / 2 + 23);
		tipRect.top = (int) (tipLineY - HALF_TIP_HEIGHT);
		tipRect.bottom = (int) (tipLineY + HALF_TIP_HEIGHT);

		tipRectF.set(tipRect);

		float textX = centerX - textWidth / 2;
		int textHeight = textBounds.bottom - textBounds.top;

		float textY = tipLineY + textHeight / (float) 2 - 3;

		canvas.drawLine(chartRect.left, tipLineY, chartRect.right, tipLineY, tipLinePaint);
		canvas.drawRoundRect(tipRectF, HALF_TIP_HEIGHT, HALF_TIP_HEIGHT, tipPaint);
		canvas.drawText(text, textX, textY, tipTextPaint);
	}

	public Paint getBorderPaint() {
		return borderPaint;
	}

	public Paint getChartBgPaint() {
		return chartBgPaint;
	}

	public Paint getCurvePaint() {
		return curvePaint;
	}

	public Paint getFillPaint() {
		return fillPaint;
	}

	public Paint getGridPaint() {
		return gridPaint;
	}

	public Paint getLabelPaint() {
		return labelPaint;
	}

	public float getTextHeight(Paint textPaint) {
		FontMetrics fm = textPaint.getFontMetrics();
		return (float) Math.ceil(fm.descent - fm.ascent) - 2;
	}

	public float getTextWidth(Paint textPaint, String text) {
		return textPaint.measureText(text);
	}

	public Paint getTipLinePaint() {
		return tipLinePaint;
	}

	public Paint getTipPaint() {
		return tipPaint;
	}

	public Paint getTipTextPaint() {
		return tipTextPaint;
	}

	public void init(List<Point> originalList, String[] labels, String tipText) {
		this.originalList = originalList;
		this.labels = labels;
		this.tipText = tipText;
		adjustedPoints = new Point[originalList.size()];
		// order by x coodinate ascending
		Collections.sort(originalList, Point.X_COMPARATOR);
		super.invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		getDrawingRect(chartRect);

		Log.d(TAG, chartRect.toString());

		if (originalList != null) {
			// mockPoints(width, height);
			drawLabels(canvas);

			int chartHeight = chartRect.bottom - chartRect.top;
			int chartWidth = chartRect.right - chartRect.left;

			adjustPoints(chartWidth, chartHeight);

			drawGrid(canvas, chartWidth);
			drawCurve(canvas, chartWidth, chartHeight);

			if (tipText != null) {
				drawTip(canvas, chartWidth, chartHeight);
			}
			canvas.drawRect(chartRect, borderPaint);
		}
	}

	public void setBorderPaint(Paint borderPaint) {
		this.borderPaint = borderPaint;
	}

	public void setChartBgPaint(Paint chartBgPaint) {
		this.chartBgPaint = chartBgPaint;
	}

	public void setCurvePaint(Paint curvePaint) {
		this.curvePaint = curvePaint;
	}

	public void setFillPaint(Paint fillPaint) {
		this.fillPaint = fillPaint;
	}

	public void setGridPaint(Paint gridPaint) {
		this.gridPaint = gridPaint;
	}

	public void setLabelPaint(Paint labelPaint) {
		this.labelPaint = labelPaint;
	}

	public void setTipLinePaint(Paint tipLinePaint) {
		this.tipLinePaint = tipLinePaint;
	}

	public void setTipPaint(Paint tipPaint) {
		this.tipPaint = tipPaint;
	}

	public void setTipTextPaint(Paint tipTextPaint) {
		this.tipTextPaint = tipTextPaint;
	}
}
