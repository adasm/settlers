// By Adam Micha³owski (c) 2013 Settlers Simulation

package com.softsquare.settlers;

import java.util.ArrayList;

import Jama.Matrix;

import com.badlogic.gdx.math.Vector2;
import com.softsquare.settlers.ConsoleVariables.VariableBoolean;
import com.softsquare.settlers.ConsoleVariables.VariableDouble;
import com.softsquare.settlers.Places.Place;
import com.softsquare.settlers.Places.Resource;
import com.softsquare.settlers.Places.Warehouse;

public class InteriorPoint implements Runnable {
	public double x0, y0;
	double r, r2;
	double a[];
	double p[];
	int N;
	double z[];
	double gamma;
	Warehouse origin;
	
	public InteriorPoint(Warehouse origin, ArrayList<Resource> resources) {
		this.origin = origin;
		x0 = origin.x;
		y0 = origin.y;
		r = origin.radius;
		r2 = r*r;
		N = resources.size() + 1;
		z = new double[2 * N];
		a = new double[N];
		p = new double[N];	
		z[0] = x0;
		z[1] = y0;
		a[0] = 0;
		p[0] = r;
		for(int i = 1; i < N; i++) {
			z[2*i + 0] = resources.get(i-1).x;
			z[2*i + 1] = resources.get(i-1).y;
			a[i] = resources.get(i-1).omega * resources.get(i-1).cost;
			p[i] = resources.get(i-1).radius;
		}
	}
	
	double dist2(double x0, double y0, double x1, double y1) {
		return (x0 - x1) * (x0 - x1) + (y0 - y1) * (y0 - y1);
	}
	
	double dist(double x0, double y0, double x1, double y1) {
		return Math.sqrt(dist2(x0, y0, x1, y1));
	}	

	double dstx(Matrix x, int i) {
		return dist (x.get(2 * i + 0, 0), x.get(2 * i + 1, 0), x.get(0, 0), x.get(1, 0));
	}

	double dstx2(Matrix x, int i) {
		return dist2(x.get(2 * i + 0, 0), x.get(2 * i + 1, 0), x.get(0, 0), x.get(1, 0));
	}

	double dstz(Matrix x, int i) {
		return dist (x.get(2 * i + 0, 0), x.get(2 * i + 1, 0), z[2 * i + 0], z[2 * i + 1]);
	}

	double dstz2(Matrix x, int i) {
		return dist2(x.get(2 * i + 0, 0), x.get(2 * i + 1, 0), z[2 * i + 0], z[2 * i + 1]);
	}

	double g(double x) {
		return 0.5 * (x + Math.sqrt(x * x + gamma));
	}

	double g1(double x) {
		return 0.5 * (x / Math.sqrt(x*x + gamma) + 1);
	}

	double g2(double x) {
		return 0.5 * gamma / Math.sqrt(Math.pow(x*x + gamma, 3));
	}

	double C(Matrix x) {
		double ret = 0;
		for (int i = 1; i < N; ++i)
			ret += a[i] * g(dstz(x, i) - p[i]);
		return ret;
	}

	double G(double t, Matrix x) {
		double ret = t * C(x);
		for (int i = 1; i < N; ++i)
			ret -= Math.log(r2 - dstx2(x, i)); // sqrt
		return ret;
	}

	Matrix gradientI(double t, Matrix x, int n) {
		Matrix out = new Matrix(2, 1, 0);
		if (n == 0) {
			for (int i = 1; i < N; ++i) {
				Matrix xn = new Matrix(2, 1);
				xn.set(0, 0, x.get(2 * n + 0, 0) - x.get(0, 0));
				xn.set(1, 0, x.get(2 * n + 1, 0) - x.get(1, 0));
				double a = 2.0 / (r2 - dstx2(x, i));
				out = out.minus(xn.times(a));
			}
		} else {
			Matrix xn = new Matrix(2, 1);
			xn.set(0, 0, x.get(2 * n + 0, 0) - x.get(0, 0));
			xn.set(1, 0, x.get(2 * n + 1, 0) - x.get(1, 0));
			Matrix zn = new Matrix(2, 1);
			zn.set(0, 0, x.get(2 * n + 0, 0) - z[2 * n + 0]);
			zn.set(1, 0, x.get(2 * n + 1, 0) - z[2 * n + 1]);
			double a1 = t * a[n] * g1(dstz(x, n) - p[n]) / dstz(x, n);
			double a2 = 2 / (r2 - dstx2(x, n));
			out = (zn.times(a1)).plus(xn.times(a2));
		}
		return out;
	}

	Matrix gradient(double t, Matrix x) {
		Matrix out = new Matrix(2 * N, 1, 0);
		for (int i = 0; i < N; i++) {
			Matrix gn = gradientI(t, x, i);
			out.set(2 * i + 0, 0, gn.get(0, 0));
			out.set(2 * i + 1 ,0, gn.get(1, 0));
		}
		return out;
	}

	Matrix hessianIJ(double t, Matrix x, int i, int j) {
		Matrix out = new Matrix(2, 2, 0);
		Matrix I = Matrix.identity(2, 2);
		if (i == 0 && j == 0) {
			for (int n = 1; n < N; ++n) {
				Matrix xn = new Matrix(2, 1);
				xn.set(0, 0, x.get(2 * n + 0, 0) - x.get(0, 0));
				xn.set(1, 0, x.get(2 * n + 1, 0) - x.get(1, 0));
				double a1 = 4.0 / (Math.pow(r2 - dstx2(x, n), 2));
				Matrix a2 = xn.times(xn.transpose());
				double a3 = 2.0 / (r2 - dstx2(x, n));
				a2 = a2.times(a1);
				Matrix a4 = I.times(a3);
				out = out.plus(a2.plus(a4));
			}
		} else if (i == 0 || j == 0) {
			int n = (i == 0 ? j : i);
			Matrix xn = new Matrix(2, 1);
			xn.set(0, 0, x.get(2 * n + 0, 0) - x.get(0, 0));
			xn.set(1, 0, x.get(2 * n + 1, 0) - x.get(1, 0));
			double a1 = 4.0 / (Math.pow(r2 - dstx2(x, n), 2));
			Matrix a2 = xn.times(xn.transpose());
			double a3 = 2.0 / (r2 - dstx2(x, n));
			a2 = a2.times(a1);
			Matrix a4 = I.times(a3);
			out = (a2.plus(a4)).times(-1.0);
		} else if (i == j) {
			int n = i;
			Matrix zn = new Matrix(2, 1);
			zn.set(0, 0, x.get(2 * n + 0, 0) - z[2 * n + 0]);
			zn.set(1, 0, x.get(2 * n + 1, 0) - z[2 * n + 1]);
			Matrix xn = new Matrix(2, 1);
			xn.set(0, 0, x.get(2 * n + 0, 0) - x.get(0, 0));
			xn.set(1, 0, x.get(2 * n + 1, 0) - x.get(1, 0));
			double a1 = (t * a[n] / dstz2(x, n)) * g2(dstz(x, n) - p[n]);
			Matrix a2 = zn.times(zn.transpose());
			a2 = a2.times(a1);
			double b1 = (t * a[n] / Math.pow(dstz(x, n), 3)) * g1(dstz(x, n) - p[n]);
			Matrix b2 = zn.times(zn.transpose());
			b2 = b2.times(b1);
			double c1 = (t * a[n] / (dstz(x, n)) * g1(dstz(x, n) - p[n]));
			Matrix c2 = I.times(c1);
			double d1 = 4.0 / Math.pow(r2 - dstx2(x, n), 2);
			Matrix d2 = xn.times(xn.transpose());
			d2 = d2.times(d1);
			double e1 = 2.0 / (r2 - dstx2(x, n));
			Matrix e2 = I.times(e1);
			out = (((a2.minus(b2)).plus(c2)).plus(d2)).plus(e2);
		} 
		return out;
	}

	Matrix hessian(double t, Matrix x) {
		Matrix out = new Matrix(2*N, 2*N);
		for (int i = 0; i < N; ++i) {
			for (int j = 0; j < N; ++j) {
				Matrix h = hessianIJ(t, x, i, j);
				out.set(2 * i + 0, 2 * j + 0, h.get(0, 0));
				out.set(2 * i + 1, 2 * j + 0, h.get(1, 0));
				out.set(2 * i + 0, 2 * j + 1, h.get(0, 1));
				out.set(2 * i + 1, 2 * j + 1, h.get(1, 1));
			}
		}
		return out;
	}
	
	double newtonCondition(double t, Matrix x) {
		Matrix g = gradient(t, x);
		Matrix gT = gradient(t, x).transpose();
		Matrix h1 = hessian(t, x).inverse();
		Matrix o = ((gT.times(h1)).times(g));
		double ret = o.get(0, 0);
		return ret;
	}
	
	Matrix dX(double t, Matrix x) {
		Matrix g = gradient(t, x);
		Matrix h1 = hessian(t, x).inverse();
		return (h1.times(g)).times(-1);
	}
	
	boolean btLineSearchCondition(double t, Matrix x, Matrix dx, double step, double a, double b) {
		double left = G(t, x.plus(dx.times(step)));
		double Fx = G(t, x);
		Matrix gT = gradient(t, x).transpose();
		Matrix o = gT.times(dx);
		double right = (Fx + a*o.get(0,0));
		return Double.isNaN(left) || left >= right;
	}
	
	double btLineSearch(double t, Matrix x, Matrix dx, double a, double b) {
		double step = 1;
		double minStep = Globals.mathMinStep.get();
		while(btLineSearchCondition(t, x, dx, step, a, b) && step > minStep && !forceStop)
			step = step * b;
		return step;
	}

	Matrix newton(double t, Matrix x, double epsilon, double a, double b) {
		int maxNewtonSteps = Globals.maxNewtonSteps.get().intValue();
		while(newtonCondition(t, x) >= epsilon && newtonSteps <= maxNewtonSteps && !forceStop) {
			Matrix dx = dX(t, x);
			double step = btLineSearch(t, x, dx, a, b);
			x = x.plus(dx.times(step));
			updateStats(x);
			putTrack(x);
			newtonSteps++;
		}
		return x;
	}
	
	Matrix interiorPoint(double t, double mi, Matrix x, double epsilon, double a, double b) {
		int maxInteriorSteps = Globals.maxInteriorSteps.get().intValue();
		while((N-1)/t >= epsilon && interiorPointSteps <= maxInteriorSteps && !forceStop) {
			putTrack(x);
			x = newton(t, x, epsilon, a, b);
			updateStats(x);
			t = mi * t;
			interiorPointSteps++;
		}
		return x;
	}
	
	int trackedNum = 0;
	int totalTrackedNum = 0;
	
	void putTrack(Matrix x) {
		synchronized(trackPlaces) {
			totalTrackedNum++;
			boolean shouldPut = true;
			if(Globals.traceElimination.get()) {
				if(trackPlaces.size() > N) {
					int s = trackPlaces.size() - 1;
					for(int i = 0; i < N; i++) {
						Place p = trackPlaces.get(s - N + 1 + i);
						if(dist(p.x, p.y, x.get(2*i + 0, 0), x.get(2*i + 1, 0)) < Globals.traceEpsilon.get().floatValue()) {
							shouldPut = false;
							break;
						}
					}
				} 
			}
			if(shouldPut) {
				trackedNum++;
				for(int i = 0; i < N; i++) {
					Place p = new Place();
					p.x = (float) x.get(2*i + 0, 0);
					p.y = (float) x.get(2*i + 1, 0);
					p.radius = Globals.factoryRadius.get().floatValue();
					trackPlaces.add(p);
				}
			}
		}
	}
	
	
	void putFinal(Matrix x) {
		synchronized(finalPlaces) {
			for(int i = 0; i < N; i++) {
				Place p = new Place();
				p.x = (float) x.get(2*i + 0, 0);
				p.y = (float) x.get(2*i + 1, 0);
				p.radius = Globals.factoryRadius.get().floatValue();
				finalPlaces.add(p);
			}
		}
	}
	
	public ArrayList<Place> trackPlaces = new ArrayList<Place>();
	public ArrayList<Place> finalPlaces = new ArrayList<Place>();
	public int interiorPointSteps = 0;
	public int newtonSteps = 0;
	public double avgNewtonSteps = 0;
	public double finalCost = -1;
	public static boolean forceStop = false;
	public static boolean needForceStop = false;
	public static boolean finished = false;
	public static boolean hadException = false;
	public static Exception exception = null;
	public boolean paused = false;
	public float startTime = 0;
	public float time = 0;
	
	void updateStats(Matrix x) {
		avgNewtonSteps = 1.0 * newtonSteps / interiorPointSteps;
		double cost = C(x);
		if(Globals.forceStopEnabled.get()) {
			if(Globals.forceStopPercentEnabled.get()) {
				if(cost >= finalCost || ((finalCost - cost)/(finalCost)) < finalCost*Globals.forceStopPercent.get().floatValue())
					forceStop = true;
			} else {
				if(cost >= finalCost)
					forceStop = true;
			}
		}
		if(needForceStop) {
			needForceStop = false;
			forceStop = true;
		}
		if(cost >= finalCost) forceStop = true;
		finalCost = cost;
		time = System.nanoTime() - startTime;
		x0 = x.get(0, 0);
		y0 = x.get(1, 0);
		origin.x = (float) x0;
		origin.y = (float) y0;
	}
	
	void compute() {
		trackPlaces.clear();
		finalPlaces.clear();
		interiorPointSteps = 0;
		newtonSteps = 0;
		avgNewtonSteps = 0;
		finalCost = Float.MAX_VALUE;
		trackedNum = 0;
		totalTrackedNum = 0;
				
		if(N > 0) {
			Matrix x = new Matrix(2*N,1);
			float rad = 1;
			x.set(0, 0, x0);
			x.set(1, 0, y0);
			for(int i = 1; i < N; i++) {
				Vector2 d = new Vector2((float)z[2*i + 0] - (float)x0, (float)z[2*i + 1] - (float)y0);
				d.nor();
				x.set(2*i + 0, 0, x0 + d.x * rad);
				x.set(2*i + 1, 0, y0 + d.y * rad);
			}
			double t0 = Globals.mathT0.get();
			double mi = Globals.mathMi.get();
			double epsilon = Globals.mathEpsilon.get();
			double a = Globals.mathAlpha.get();
			double b = Globals.mathBeta.get();
			gamma = Globals.mathGamma.get();
			x = interiorPoint(t0, mi, x, epsilon, a, b);
			x0 = x.get(0, 0);
			y0 = x.get(1, 0);
			origin.x = (float) x0;
			origin.y = (float) y0;
			updateStats(x);
			putFinal(x);
			Logger.logInfo("Computed. InteriorPoint");
		}
		else Logger.logInfo("Not computed. N = 0");
	}
	
	@Override
	public void run() {
		time = 0;
		startTime = System.nanoTime();
		hadException = false;
		exception = null;
		finished = false;
		try {
			compute();
		} catch (Exception e) {
			hadException = true;
			exception = e;
		}
		finished = true;
	}
	
	static public Thread thread = null;
	
	@SuppressWarnings("deprecation")
	public void stop() {
		
		if(thread != null) {
			if(paused)
				pauseResume();
			forceStop = true;
			try {
				thread.join(5000);
			} catch (InterruptedException e) {
				thread.destroy();
			}
			thread = null;
		}
	}
	
	
	public void recompute() {
		stop();
		forceStop = false;
		needForceStop = false;
		thread = new Thread(this);
		paused = false;
		thread.start();
	}

	@SuppressWarnings("deprecation")
	public void pauseResume() {
		if(thread != null) {
			if(thread.isAlive()) {
				if(paused) {
					thread.resume();
					paused = false;
				} else {
					thread.suspend();
					paused = true;
				}
			} 
		}
		
	}
	
}
