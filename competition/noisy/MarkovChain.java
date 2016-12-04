package competition.noisy;

import java.util.Arrays;

/**
 * User: Matt Grant 
 * Date: 30 Nov 2016
 * Package: competition.noisy
 */
// This class implements the markov chain used to estimate noise
// It can be used both with and without a prior
// This only handles boolean transition and sensor models
// Probablity arrays are always [True, False]
 
public class MarkovChain{
	private float[] _prior = {.5f, .5f};
	private float[] _sensor;
	private float[] _trans;
	private Node _bcurr = null;
	private Node _bprev = null;
	private Node _curr = null;
	private Node _prev = null;
	
	public MarkovChain(float[] sensor, float[] trans, float[] prior){
		_prior = prior;
		_sensor = sensor;
		_trans = trans;	
	}
	
	public MarkovChain(float[] sensor, float[] trans){
		_sensor = sensor;
		_trans = trans;	
	}
	
	public void observe(boolean obs){
		if(_curr == null){
			// Start a chain
			_curr = new Node(obs, _prior);
		}
		else{
			_prev = _curr;
			_curr = new Node(obs, _prev.prob());
		}
	}
	
	public float estimate(){
		if(_curr != null){
			return _curr.prob()[0];
		}
		return _prior[0];
	}
	
	public float predict(int steps){
		float[] temp =_curr.predict(steps);
		return temp[0];
	}
	
	public void backup(){
		try{
			if(_curr != null){
				_bcurr = (Node) _curr.clone();
			}
			else{
				System.out.println("Markov chain not init before backup.");
			}
			if(_prev != null){
				_bprev = (Node) _prev.clone();
			}
		}
		catch(CloneNotSupportedException e){
			throw new AssertionError(e);
		}
	}
	
	public void restore(){
		if(_bcurr != null){
			_curr = _bcurr;
		}
		else{
			System.out.println("Restore before backup.");
		}
		if(_bprev != null){
			_prev = _bprev;
		}	
	}
	
	public void print(){
		System.out.println("Prior:    " + Arrays.toString(_prior));
		System.out.println("Tmodel:   " + Arrays.toString(_trans));
		System.out.println("Smodel:   " + Arrays.toString(_sensor));
		if(_curr != null){
			System.out.println("Current:  " + _curr.print());
		}
		if(_prev != null){
			System.out.println("Previous: " + _prev.print());
		}
	}
	
	private class Node implements Cloneable{
		private boolean _obs;
		private float _prob[];
		
		public Node(boolean obs, float[] prior){
			_obs = obs;
			calcProb(prior);
		}
		
		private void calcProb(float[] prior){
			float[] prob = {prior[0], prior[1]};
			prob = trans(prob);
			prob = sense(prob);
			_prob = prob;
		}
		
		// Transition model is 
		// [ P(Event | PEvent)P(PEvent), P(Event | !PEvent)P(!PEvent) ]
		private float[] trans(float[] prior){
			float[] toReturn = {0,0};
			toReturn[0] = (prior[0] * _trans[0])     + (prior[1] * _trans[1]);
			toReturn[1] = (prior[0] * (1 - _trans[0])) + (prior[1] * (1 - _trans[1]));
	
			return toReturn;
		}
		
		// Sensor model is
		// aP(Obs | E!E)P(E!E) where E!E is <E, !E> and a is a normalizing constant
		private float[] sense(float[] prior){
			float t;
			float f;
			if(_obs){
				t = _sensor[0];
				f = _sensor[1];
			}
			else{
				t = 1 - _sensor[0];
				f = 1 - _sensor[1];
			}
			float[] toReturn = {prior[0] * t, prior[1] * f};
			float a = toReturn[0] + toReturn[1];
			toReturn[0] *= (1/a);
			toReturn[1] *= (1/a);
			return toReturn;
		}
		
		private float[] predict(int repeat){
			// Create an estimation node and run the trans model
			float[] toReturn = {_prob[0], _prob[1]};
			for(int i = 0; i < repeat; i++){
				toReturn = trans(toReturn);
			}
			return toReturn;
			
		}
		
		private float[] prob(){
			return _prob;
		}
		
		public boolean obs(){
			return _obs;
		}
		
		private String print(){
			return new String( "[" + _prob[0] + ", " + _prob[1] + "]");
		}
		
		protected Object clone() throws CloneNotSupportedException{
			Node nc;
			try{
				nc =(Node) super.clone();
			}
			catch (CloneNotSupportedException e){
				// apparently this signals that this line should never be reached
				throw new AssertionError(e);
			}
			float[] nProb = new float[_prob.length];
			for(int i = 0; i < _prob.length; i++){
				nProb[i] = _prob[i];
			}
			nc._prob = nProb;
			return nc;
		}
	}
}
