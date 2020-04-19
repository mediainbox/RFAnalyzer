package com.mantz_it.rfanalyzer;

import android.app.Application;
import android.os.Debug;
import android.test.ApplicationTestCase;

import java.nio.ByteBuffer;
import java.sql.SQLOutput;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

	@Override
	public void setUp() throws Exception {
		super.setUp();

	}

	public void testFirFilter() {
		int samples = 128;
		float[] reIn = new float[samples];
		float[] imIn = new float[samples];
		float[] reOut = new float[samples/4];
		float[] imOut = new float[samples/4];
		int sampleRate = 1000;
		SamplePacket in = new SamplePacket(reIn, imIn,0, sampleRate);
		SamplePacket out = new SamplePacket(reOut, imOut,0, sampleRate/4);
		out.setSize(0);
		int f1 = 50;
		int f2 = 200;

		for (int i = 0; i < reIn.length; i++) {
			reIn[i] = (float) Math.cos(2 * Math.PI * f1 * i/ (float)sampleRate) + (float) Math.cos(2 * Math.PI * f2 * i/ (float)sampleRate);
			imIn[i] = (float) Math.sin(2 * Math.PI * f1 * i/ (float)sampleRate) + (float) Math.sin(2 * Math.PI * f2 * i/ (float)sampleRate);
		}

		FirFilter filter = FirFilter.createLowPass(4, 1, sampleRate, 100, 50, 60);
		System.out.println("Created filter with " + filter.getNumberOfTaps() + " taps!");

		FFT fft1 = new FFT(samples);

		System.out.println("Before FILTER:");
		spectrum(fft1, reIn, imIn);

		filter.filter(in, out, 0, in.size());

		FFT fft2 = new FFT(samples/4);

		System.out.println("After FILTER:");
		spectrum(fft2, reOut, imOut);
	}

	public void testComplexFirFilter() {
		int samples = 32;
		float[] reIn = new float[samples];
		float[] imIn = new float[samples];
		float[] reOut = new float[samples];
		float[] imOut = new float[samples];
		int sampleRate = 1000;
		SamplePacket in = new SamplePacket(reIn, imIn,0, sampleRate);
		SamplePacket out = new SamplePacket(reOut, imOut,0, sampleRate);
		out.setSize(0);
		int f1 = 250;
		int f2 = -250;

		for (int i = 0; i < reIn.length; i++) {
			reIn[i] = (float) Math.cos(2 * Math.PI * f1 * i/ (float)sampleRate) + (float) Math.cos(2 * Math.PI * f2 * i/ (float)sampleRate);
			imIn[i] = (float) Math.sin(2 * Math.PI * f1 * i/ (float)sampleRate) + (float) Math.sin(2 * Math.PI * f2 * i/ (float)sampleRate);
		}

		ComplexFirFilter filter = ComplexFirFilter.createBandPass(1, 1, sampleRate, -450, -50, 50, 60);
		System.out.println("Created filter with " + filter.getNumberOfTaps() + " taps!");

		FFT fft1 = new FFT(samples);

		System.out.println("Before FILTER:");
		spectrum(fft1, reIn, imIn);

		filter.filter(in, out, 0, in.size());

		FFT fft2 = new FFT(samples);

		System.out.println("After FILTER:");
		spectrum(fft2, reOut, imOut);
	}

	public void testComplexFirFilter2() {
		int samples = 32;
		float[] reIn = new float[samples];
		float[] imIn = new float[samples];
		float[] reOut = new float[samples];
		float[] imOut = new float[samples];
		int sampleRate = 1000;
		SamplePacket in = new SamplePacket(reIn, imIn,0, sampleRate);
		SamplePacket out = new SamplePacket(reOut, imOut,0, sampleRate);
		out.setSize(0);

		reIn[0] = 1;
		imIn[0] = 1;
		for (int i = 1; i < reIn.length; i++) {
			reIn[i] = 0;
			imIn[i] = 0;
		}

		ComplexFirFilter filter = ComplexFirFilter.createBandPass(1, 1, sampleRate, 50, 450, 50, 60);
		System.out.println("Created filter with " + filter.getNumberOfTaps() + " taps!");

		FFT fft1 = new FFT(samples);

		System.out.println("Before FILTER:");
		spectrum(fft1, reIn, imIn);

		filter.filter(in, out, 0, in.size());

		FFT fft2 = new FFT(samples);

		System.out.println("After FILTER:");
		spectrum(fft2, reOut, imOut);
	}

	public void testFirFilterPerformance() {
		int sampleRate = 4000000;
		int packetSize = 16384;
		int loopCycles = 1000;
		SamplePacket in = new SamplePacket(packetSize);
		SamplePacket out = new SamplePacket(packetSize);
		out.setSize(0);
		in.setSize(in.capacity());

		//Debug.startMethodTracing("FirFilter");
		FirFilter filter = FirFilter.createLowPass(4, 1, sampleRate, 100000, 800000, 40);
		System.out.println("Created filter with " + filter.getNumberOfTaps() + " taps!");

		System.out.println("##### START ...");
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < loopCycles; i++) {
			filter.filter(in, out, 0, in.size());
			out.setSize(0);
		}
		System.out.println("##### DONE. Time needed for 1 sec of samples: " + (System.currentTimeMillis() - startTime)/(packetSize*loopCycles/4000000.0));
		//Debug.stopMethodTracing();
	}

	public void testHalfBandLowPassFilter() {
		int samples = 128;
		float[] reIn = new float[samples];
		float[] imIn = new float[samples];
		float[] reOut = new float[samples/2];
		float[] imOut = new float[samples/2];
		int sampleRate = 1000;
		SamplePacket in = new SamplePacket(reIn, imIn,0, sampleRate);
		SamplePacket out = new SamplePacket(reOut, imOut,0, sampleRate/2);
		out.setSize(0);
		int f1 = 50;
		int f2 = 400;

		for (int i = 0; i < reIn.length; i++) {
			reIn[i] = (float) Math.cos(2 * Math.PI * f1 * i/ (float)sampleRate) + (float) Math.cos(2 * Math.PI * f2 * i/ (float)sampleRate);
			imIn[i] = (float) Math.sin(2 * Math.PI * f1 * i/ (float)sampleRate) + (float) Math.sin(2 * Math.PI * f2 * i/ (float)sampleRate);
		}

		HalfBandLowPassFilter halfBandLowPassFilter = new HalfBandLowPassFilter(12);
		assertEquals(halfBandLowPassFilter.filterN12(in, out, 0, in.size()), in.size());

		FFT fft1 = new FFT(samples);
		System.out.println("Before FILTER:");
		spectrum(fft1, reIn, imIn);

		FFT fft2 = new FFT(samples/2);
		System.out.println("After FILTER:");
		spectrum(fft2, reOut, imOut);
	}

	public void testHalfBandLowPassFilterPerformance() {
		int sampleRate = 1000000;
		int packetSize = 16384;
		int loopCycles = 1000;
		long startTime;
		int firFilterTime;
		int halfBandFilterTime;
		SamplePacket in = new SamplePacket(packetSize);
		SamplePacket out = new SamplePacket(packetSize);
		out.setSize(0);
		in.setSize(in.capacity());

		// Compare with equal FirFilter:
		FirFilter filter = FirFilter.createLowPass(2, 1, sampleRate, 100000, 300000, 30);
		System.out.println("FirFilter for comparing has " + filter.getNumberOfTaps() + " taps!");

		startTime = System.currentTimeMillis();
		for (int i = 0; i < loopCycles; i++) {
			filter.filter(in, out, 0, in.size());
			out.setSize(0);
		}
		firFilterTime = (int)(System.currentTimeMillis() - startTime);
		System.out.println("Time needed by FirFilter for 1 sec of samples: " + firFilterTime/(packetSize*loopCycles/(float)sampleRate));

		// Now the same for the actual half band filter:
		HalfBandLowPassFilter halfBandLowPassFilter = new HalfBandLowPassFilter(12);
		startTime = System.currentTimeMillis();
		for (int i = 0; i < loopCycles; i++) {
			halfBandLowPassFilter.filterN12(in, out, 0, in.size());
			out.setSize(0);
		}
		halfBandFilterTime = (int)(System.currentTimeMillis() - startTime);
		System.out.println("Time needed by HalfBandLowPassFilter for 1 sec of samples: " + halfBandFilterTime/(packetSize*loopCycles/(float)sampleRate));
		System.out.println("Half band filter is " + ((firFilterTime - halfBandFilterTime)/(float)halfBandFilterTime)*100 + "% faster than the FirFilter!");

		// just for fun: see how the N8 filter performs with filterN8 and with filter:
		halfBandLowPassFilter = new HalfBandLowPassFilter(12);
		startTime = System.currentTimeMillis();
		for (int i = 0; i < loopCycles; i++) {
			halfBandLowPassFilter.filterN12(in, out, 0, in.size());
			out.setSize(0);
		}
		halfBandFilterTime = (int)(System.currentTimeMillis() - startTime);
		System.out.println("Time needed by filterN8 HalfBandLowPassFilter for 1 sec of samples: " + halfBandFilterTime/(packetSize*loopCycles/(float)sampleRate));

		halfBandLowPassFilter = new HalfBandLowPassFilter(12);
		startTime = System.currentTimeMillis();
		for (int i = 0; i < loopCycles; i++) {
			halfBandLowPassFilter.filter(in, out, 0, in.size());
			out.setSize(0);
		}
		halfBandFilterTime = (int)(System.currentTimeMillis() - startTime);
		System.out.println("Time needed by filter HalfBandLowPassFilter for 1 sec of samples: " + halfBandFilterTime/(packetSize*loopCycles/(float)sampleRate));
	}

	public void testFFT() throws Exception {
		// Test the FFT to make sure it's working
		int N = 8;

		FFT fft = new FFT(N);

		float[] window = fft.getWindow();
		float[] re = new float[N];
		float[] im = new float[N];

		// Impulse
		re[0] = 1; im[0] = 0;
		for(int i=1; i<N; i++)
			re[i] = im[i] = 0;
		beforeAfter(fft, re, im);

		// Nyquist
		for(int i=0; i<N; i++) {
			re[i] = (float)Math.pow(-1, i);
			im[i] = 0;
		}
		beforeAfter(fft, re, im);

		// Single sin
		for(int i=0; i<N; i++) {
			re[i] = (float)Math.cos(2*Math.PI*i / N);
			im[i] = 0;
		}
		beforeAfter(fft, re, im);

		// Ramp
		for(int i=0; i<N; i++) {
			re[i] = i;
			im[i] = 0;
		}
		beforeAfter(fft, re, im);

		long time = System.currentTimeMillis();
		double iter = 30000;
		for(int i=0; i<iter; i++)
			fft.fft(re,im);
		time = System.currentTimeMillis() - time;
		System.out.println("Averaged " + (time/iter) + "ms per iteration");
	}

	protected static void beforeAfter(FFT fft, float[] re, float[] im) {
		System.out.println("Before: ");
		printReIm(re, im);
		//fft.applyWindow(re, im);
		fft.fft(re, im);
		System.out.println("After: ");
		printReIm(re, im);
	}

	protected static void printReIm(float[] re, float[] im) {
		System.out.print("Re: [");
		for(int i=0; i<re.length; i++)
			System.out.print(((int)(re[i]*1000)/1000.0) + " ");

		System.out.print("]\nIm: [");
		for(int i=0; i<im.length; i++)
			System.out.print(((int)(im[i]*1000)/1000.0) + " ");

		System.out.println("]");
	}

	protected static void spectrum(FFT fft, float[] re, float[] im) {
		//fft.applyWindow(re, im);
		int length = re.length;
		float[] reDouble = new float[length];
		float[] imDouble = new float[length];
		float[] mag = new float[length];
		for (int i = 0; i < length; i++) {
			reDouble[i] = re[i];
			imDouble[i] = im[i];
		}

		fft.fft(reDouble, imDouble);
		// Calculate the logarithmic magnitude:
		for (int i = 0; i < length; i++) {
			// We have to flip both sides of the fft to draw it centered on the screen:
			int targetIndex = (i+length/2) % length;

			// Calc the magnitude = log(  re^2 + im^2  )
			// note that we still have to divide re and im by the fft size
			mag[targetIndex] = (float) Math.log(Math.pow(reDouble[i]/fft.n,2) + Math.pow(imDouble[i]/fft.n,2));
		}

		System.out.print("Spectrum: [");
		for (int i = 0; i < length; i++) {
			System.out.print(" " + (int) mag[i]);
		}
		System.out.println("]");
	}

}