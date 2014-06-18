package com.wagoodman.stackattack;

import android.util.FloatMath;


/**
 * This class applies F(t) given the correct parameters. F(t) can be described as such:
 * 
 *  	F(t) = Y(t)*(endPoint - startPoint)
 * 
 * Where Y(t=0)=0, Y(t=duration)=1 and F(t=0)=0, F(t=duration)=(endPoint-startPoint). 
 * F describes position over time (generally between the start and end points). The overall displacement should be
 * scaled to (endPoint - startPoint). Eg. Given startPoint=25 and endPoint=55, F(t=duration)=30.
 * 
 * Each enum represents an equation (described by the name). The function 'apply' is used as a facade to 
 * evaluate the mathematical function ( F(t) ) with given input parameters:
 * 
 * @param select		the enum value used to select the equation to evaluate with
 * @param t				a time between 0 and duration that F(t) is evaluated at
 * @param duration		the maximum t-value for F(t); or the period of F(t)
 * @param startPoint	the magnitude at t = 0; 
 * 						NOTE: this is not necessarily a min/max magnitude for F(t) between t=0 and t=duration
 * @param endPoint		the magnitude at t = duration; 
 * 						NOTE: this is not necessarily a min/max magnitude for F(t) between t=0 and t=duration
 *
 * 
 * @author Alex Goodman
 *
 */

public enum MotionEquation
{
	
	LINEAR,
	SIN,
	LOGISTIC,
	SPRING_UNDAMP_P4,
	SPRING_UNDAMP_P6,
	SPRING_UNDAMP_ALT_K9_5,
	SPRING_UNDAMP_ALT_K9_3,
	SPRING_UNDAMP_ALT_K4_5,
	LOGISTIC_OVERSHOOT_SLOW,
	LOGISTIC_OVERSHOOT_FAST,
	SIN_OVERSHOOT_SLOW,
	SIN_OVERSHOOT_FAST
	;
	
	private static final float PI = (float) Math.PI;
	
	MotionEquation()
	{

	}

	
	public static double applyFinite(TransformType transform, MotionEquation eq, long t, double duration, double startPoint, double endPoint)
	{
		
		
		switch (eq)
		{

		case LINEAR: 
			/*
				#LINEAR
				duration = 500
				start = 25
				end = 55
				fn = (t/duration)
				
				print float(fn(t=duration)) #= ~1
				print float(fn(t=0) ) #= ~0
				
				p = fn*(end-start)
				p.plot((t,0,duration))
			*/
			
			// Scale / Color / Rotate / Transform
			return startPoint + (t/(double)duration)*(endPoint-startPoint);

		
		case SIN: 
			/*	
				#SIN
				duration = 500
				start = 25
				end = 55
				A = 1/2
				w1 = ((2*pi)/(2*duration))
				fn = A*(sin(w1*(t-(duration/2.0))) + 1)
				
				print float(fn(t=duration)) #= ~1
				print float(fn(t=0) ) #= ~0
				
				p = fn*(end-start)
				plot(p, (t,0, duration))
			*/
						
			// Scale / Color / Rotate / Translate
			return startPoint + (0.5*(Math.sin( ((2.0*PI)/(2.0*duration))*(t - (duration/2.0))) + 1 ))*(endPoint - startPoint);

	

			
		case LOGISTIC: 
			/*
			#LOGISTIC
			duration = 500
			start = 25
			end = 55
			k = 2
			po = 1
			r = (1/(duration/8.0))
			fn = ((((k*po*e^(r*(t - (duration/2))))/(k + po*(e^(r*(t - (duration/2)))-1))) - po  + (k/2))*(1/2))*1.035 - 0.018
			
			print float(fn(t=duration)) #= ~1
			print float(fn(t=0) ) #= ~0
			
			p = fn*(end-start)
			p.plot((t,0,duration))
			*/
			
			return startPoint + (endPoint - startPoint)*
					(((((2*Math.exp((1/(duration/8.0))*(t - (duration/2.0)))) / (2 + (Math.exp((1/(duration/8.0))*(t - (duration/2.0)))-1))))*(0.5))*1.035 - 0.018);


		case SPRING_UNDAMP_P4: 
			/*	
			#Spring Undamp 4
			duration = 500
			start = 25
			end = 55
			wd = 10.584055/duration
			s = .4
			wn = wd/sqrt(1-s^2)
			fn = (wn^2)*(1/(wn^2))*( 1 - ((e^(-s*wn*t))/(sqrt(1-(s^2))))*cos(wd*t + 0.4115192) )
			
			print float(fn(t=duration)) #= ~1
			print float(fn(t=0) ) #= ~0
			
			p = fn*(end-start)
			plot(y, (t,0, duration))
			*/
			
			return startPoint +  (endPoint - startPoint)*( (Math.pow((10.584055/duration)*(1 / Math.sqrt( 1-(0.4*0.4) )), 2))*(1/(Math.pow((10.584055/duration)*(1 / Math.sqrt( 1-(0.4*0.4) )), 2)))*( 1 - ((Math.exp(-0.4*(10.584055/duration)*(1 / Math.sqrt( 1-(0.4*0.4) ))*t)/(1 / Math.sqrt( 1-(0.4*0.4) ))) *Math.cos((10.584055/duration)*t + 0.4115192) ) ) );

		case SPRING_UNDAMP_P6: 
			/*	
				#Spring Undamp 6
				
				duration = 500
				start = 25
				end = 55
				wd = 10.584055/duration
				s = .4
				wn = wd/sqrt(1-s^2)
				fn = (wn^2)*(1/(wn^2))*( 1 - ((e^(-s*wn*t))/(sqrt(1-(s^2))))*cos(wd*t + 0.4115192) )
				
				print float(fn(t=duration)) #= ~1
				print float(fn(t=0) ) #= ~0
				
				p = fn*(end-start)
				plot(y, (t,0, duration))
			*/
			return startPoint + (endPoint - startPoint)*( (Math.pow((10.0/duration)*(1 / Math.sqrt( 1-(0.6*0.6) )),2))*(1/(Math.pow((10.0/duration)*(1 / Math.sqrt( 1-(0.6*0.6) )), 2)))*( 1 - ((Math.exp(-0.6*(10.0/duration)*(1 / Math.sqrt( 1-(0.6*0.6) ))*t)/(1 / Math.sqrt( 1-(0.6*0.6) ))) *Math.cos((10.0/duration)*t + 0.6505) ) ) );
			
		
			
			
		// High overshoot, long ring period
		case SPRING_UNDAMP_ALT_K9_5:
			
			/*
				# Spring Undamp Alt
				dur = 400
				t = var('t')
				k = 9
				m = 1
				wo = sqrt(k/m)
				T = 1/(wo)
				u = wo*sqrt(1-(T^2))
				
				startDeg = 172
				endDeg   = 270
				
				
				x = cos(u*t*(5/dur))*e^(-T*wo*(5/dur)*t)
				pos = x*(startDeg-endDeg) + endDeg
				
				
				print float(x(t=224)), float(pos(t=224))
				print float(x(t=dur))
				plot(pos, (t,0,dur))
			*/
			return endPoint + (startPoint - endPoint)*(Math.cos((3*Math.sqrt(1-Math.pow(1.0/3.0, 2)))*t*(5.0/duration))*Math.exp((-1)*(5.0/duration)*t));

			
			
			
		// High overshoot, short ring period
		case SPRING_UNDAMP_ALT_K9_3:
			
			/*
				# Spring Undamp Alt
				dur = 400
				t = var('t')
				k = 9
				m = 1
				wo = sqrt(k/m)
				T = 1/(wo)
				u = wo*sqrt(1-(T^2))
				
				startDeg = 172
				endDeg   = 270
				
				
				x = cos(u*t*(5/dur))*e^(-T*wo*(5/dur)*t)
				pos = x*(startDeg-endDeg) + endDeg
				
				
				print float(x(t=224)), float(pos(t=224))
				print float(x(t=dur))
				plot(pos, (t,0,dur))
			*/
			return endPoint + (startPoint - endPoint)*(Math.cos((3*Math.sqrt(1-Math.pow(1.0/3.0, 2)))*t*(2.975/duration))*Math.exp((-1)*(2.975/duration)*t));
		
			
			
			
		// Low overshoot, short ring period
		case SPRING_UNDAMP_ALT_K4_5:
			
			/*
				# Spring Undamp Alt
				dur = 400
				t = var('t')
				k = 4
				m = 1
				wo = sqrt(k/m)
				T = 1/(wo)
				u = wo*sqrt(1-(T^2))
				
				startDeg = 172
				endDeg   = 270
				
				
				x = cos(u*t*(5/dur))*e^(-T*wo*(5/dur)*t)
				pos = x*(startDeg-endDeg) + endDeg
				
				
				print float(x(t=224)), float(pos(t=224))
				print float(x(t=dur))
				plot(pos, (t,0,dur))
			*/
			return endPoint + (startPoint - endPoint)*(Math.cos((2*Math.sqrt(1-Math.pow(1.0/2.0, 2)))*t*(5.0/duration))*Math.exp((-1)*(5.0/duration)*t));
			
			
		case LOGISTIC_OVERSHOOT_SLOW: 
			/*	
			# Logistic Overshoot
			duration = 500
			start = 25
			end = 55
			
			# Logistic
			k = 2
			po = 1
			r = 1/(duration/8)
			lfn = ((((k*po*e^(r*(t - (duration/2))))/(k + po*(e^(r*(t - (duration/2)))-1))) - po  + (k/2))*(1/2))*1.035 - 0.018
			
			# Delta Function
			a = 1
			sizeNum = 10
			size = sizeNum/duration
			offset = 0.8*sizeNum
			bump = ((1/(a*sqrt(pi)))*e^(-( offset - (size*t)  )^2/a^2))*0.25 + 1
			
			plot( [bump*lfn*(end-start) , end-start] , (t,0, duration))
			*/
			
			
			
			// Scale / Color / Rotate / Translate
			return 
					startPoint +
					(
						// LOGISTIC
							(((((2*Math.exp((1/(duration/8.0))*(t - (duration/2.0)))) / (2 + (Math.exp((1/(duration/8.0))*(t - (duration/2.0)))-1))))*(0.5))*1.035 - 0.018) * 
						
						// Delta
						(((1.0/(float)(FloatMath.sqrt(PI)))*Math.exp(-Math.pow( 0.75*7.0 - (7.0/(float)duration*t), 2)))*0.5 + 1) *
						
						// Min-Max
						(endPoint - startPoint)
					)
					;
			
		case SIN_OVERSHOOT_SLOW: 
			/*	
			# Sin Overshoot
			duration = 500
			start = 25
			end = 55
			
			# Logistic
			A = 1/2
			w1 = ((2*pi)/(2*duration))
			fn = A*(sin(w1*(t-(duration/2.0))) + 1)
			
			# Delta Function
			a = 1
			duration = 1000
			sizeNum = 10
			finalDur = 500
			size = sizeNum/finalDur
			offset = 0.8*size*duration
			bump = ((1/(a*sqrt(pi)))*e^(-( offset - (size*t)  )^2/a^2))*0.25 + 1
			
			plot( [bump*fn*(end-start) , end-start] , (t,0, duration))
			*/
			
			// Scale / Color / Rotate / Translate
			return 
					startPoint +
					(
						// SIN
						(0.5*(Math.sin( ((2.0*PI)/(2.0*duration))*(t - (duration/2.0))) + 1 )) *
						
						// Delta
						(((1.0/(float)(FloatMath.sqrt(PI)))*Math.exp(-Math.pow( 0.75*7.0 - (7.0/(float)duration*t), 2)))*0.5 + 1) *
						
						// Min-Max
						(endPoint - startPoint)
					)
					;
			
		case SIN_OVERSHOOT_FAST:
			/*
			# Sin Overshoot
			duration = 500
			start = 25
			end = 55
			
			
			#Slope Delta
			a = 1
			sizeNum = 2
			size = sizeNum/duration
			offset = 0.2*size*duration
			bump = ((1/(a*sqrt(pi)))*e^(-( offset - (size*t)  )^2/a^2))*2 + 1
			
			# Sin
			A = 1/2
			w1 = ((2*pi)/(2*duration))
			fn = A*(sin(w1*(t-(duration/2.0))) + 1)
			
			# End Delta
			end_a = 1
			end_sizeNum = 7
			end_size = end_sizeNum/duration
			end_offset = 0.75*end_sizeNum
			end_bump = ((1/(end_a*sqrt(pi)))*e^(-( end_offset - (end_size*t)  )^2/end_a^2))*0.5 + 1
			
			
			plot([fn, fn*bump*end_bump, bump, end_bump], (t,0,duration)) 
			*/
			// Transform
			/*
			if (transform == TransformType.TRANSLATE)
				return 
						// Slope Delta
						(((1.0/(float)(FloatMath.sqrt(PI)))*Math.exp(-Math.pow( 0.2*2.0 - (2.0/(float)duration*t), 2)))*2 + 1) *
							
						// SIN
						(0.5*(Math.sin( ((2.0*PI)/(2.0*duration))*(t - (duration/2.0))) + 1 )) *
						
						// End Delta
						(((1.0/(float)(FloatMath.sqrt(PI)))*Math.exp(-Math.pow( 0.75*7.0 - (7.0/(float)duration*t), 2)))*0.5 + 1) *
						
						// Min-Max
						(endPoint - startPoint);
			*/
			
			// Scale / Color / Rotate / Translate
			return 
					startPoint +
					(
						// Slope Delta
						(((1.0/(float)(FloatMath.sqrt(PI)))*Math.exp(-Math.pow( 0.35*3.0 - (3.0/(float)duration*t), 2)))*0.75 + 1) *
							
						// SIN
						(0.5*(Math.sin( ((2.0*PI)/(2.0*duration))*(t - (duration/2.0))) + 1 )) *
						
						// End Delta
						(((1.0/(float)(FloatMath.sqrt(PI)))*Math.exp(-Math.pow( 0.75*7.0 - (7.0/(float)duration*t), 2)))*0.5 + 1) *
						
						// Min-Max
						(endPoint - startPoint)
					)
					;
			
		}
		
		// default case
		return -1;
	}
	
	
	
	
	
	public static double applyContinuous(TransformType transform, MotionEquation eq, long t, double rate, double unit, double startPoint)
	{
		
		
		switch (eq)
		{

		case LINEAR: 
			/*
			#LINEAR
			duration = 500
			start = 25
			end = 55
			fn = (t/duration)
			
			print float(fn(t=duration)) #= ~1
			print float(fn(t=0) ) #= ~0
			
			p = fn*(end-start)
			p.plot((t,0,duration))
			*/
			
			// Scale / Color / Rotate
			return startPoint + (t*(rate/unit));

		case SIN: 
			/*	
			#SIN_OSC
			
			rate = 60
			start = 0
			period = 1000
			w1 = ((2*pi)/(period))
			fn = A*(sin(w1*t) )
			
			print float(fn(t=period)) #= ~1
			print float(fn(t=0) ) #= ~0
			
			p = start + fn
			plot(p, (t,0, period))
			*/
			
			//return startPoint + (rate*Math.sin( ((2.0*PI)/unit) *(t - ((unit)/2.0))) );
			return startPoint + (rate*Math.sin( ((2.0*PI)/unit)*t));
			
		case LOGISTIC: 
			/*
			#LOGISTIC
			duration = 500
			start = 25
			end = 55
			k = 2
			po = 1
			r = 1/(duration/8)
			fn = ((((k*po*e^(r*(t - (duration/2))))/(k + po*(e^(r*(t - (duration/2)))-1))) - po  + (k/2))*(1/2))*1.035 - 0.018
			
			print float(fn(t=duration)) #= ~1
			print float(fn(t=0) ) #= ~0
			
			p = fn*(end-start)
			p.plot((t,0,duration))
			*/
			return 0;

		case SPRING_UNDAMP_P4: 
			/*	
			#Spring Undamp 4
			duration = 500
			start = 25
			end = 55
			wd = 10.584055/duration
			s = .4
			wn = wd/sqrt(1-s^2)
			fn = (wn^2)*(1/(wn^2))*( 1 - ((e^(-s*wn*t))/(sqrt(1-(s^2))))*cos(wd*t + 0.4115192) )
			
			print float(fn(t=duration)) #= ~1
			print float(fn(t=0) ) #= ~0
			
			p = fn*(end-start)
			plot(y, (t,0, duration))
			*/
			return 0;

		case SPRING_UNDAMP_P6: 
			/*	
			#Spring Undamp 6
			duration = 500
			start = 25
			end = 55
			wd = 10.584055/duration
			s = .4
			wn = wd/sqrt(1-s^2)
			fn = (wn^2)*(1/(wn^2))*( 1 - ((e^(-s*wn*t))/(sqrt(1-(s^2))))*cos(wd*t + 0.4115192) )
			
			print float(fn(t=duration)) #= ~1
			print float(fn(t=0) ) #= ~0
			
			p = fn*(end-start)
			plot(y, (t,0, duration))
			*/
			return 0;
			
		}
		
		// default case
		return -1;
	}
	
	
	
}
