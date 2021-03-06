// Matt Grant
// This is a modification of Robin Baumgarten's original code
// All searching functionality was stripped and only simulation code was left
package competition.noisy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import ch.idsia.mario.engine.sprites.Mario;
import competition.noisy.level.Level;
import competition.noisy.MarkovChain;


// Grid position is actual position / 16

// Verbosity is assigned in competition.noisy.Verbose interface
public class AStarSimulator implements Verbose{
    public LevelScene levelScene;
    public LevelScene workScene;
    private LevelScene simState = null;
    
    private float maxSpeed = 10.9090909f;
    private long timeBudget = 10000000; // ns
    private long postSearchReserve = 1000000; //ns
    private float goal = 2.0f;
    // max actual right is 176
    private int maxRight = 11;
    // Right side of screen is 352 or 22
    //////////////Markov Vars///////////////
    Random rand = new Random();

    ////////////////////Initialization///////////////////////
    public AStarSimulator(){
    	initialiseSimulator();
    }
	
    public void initialiseSimulator(){
		levelScene = new LevelScene();
		levelScene.init();	
		levelScene.level = new Level(1500,15);
		workScene = backupState();
	}
    
    ////////////////////////Search///////////////////////////  
    public ArrayList<boolean[]> getPlan(MarkovChain mc){
    	ArrayList<Node> nodes;
    	if(mc != null){
    		nodes = msearch(mc);
    	}
    	else{
    		nodes = search();
    	}
    	ArrayList<boolean[]> plan = new ArrayList<boolean[]>();
    	for(Node n : nodes){
    		for(int i = 0; i < n._repeat; i++){
    			plan.add(n._action);
    		}
    	}
    	simState = cloneState(nodes.get(nodes.size() -1).state());
    	return plan;
    }
    
    @SuppressWarnings("unused")
	public ArrayList<Node> search(){
    	long startTime = System.nanoTime();
    	long cTime;
    	// This is used to pass states between searches
    	if (simState == null){
    		workScene = backupState();
    	}
    	else{
    		workScene = simState;
    	}
    	// Standard implementation of A* with early exit
    	ArrayList<Node> path = new ArrayList<Node>();
    	HashMap<Node, Float> cost_so_far = new HashMap<Node, Float>();
    	ArrayList< Pair<Node, Float> > frontier = new ArrayList<Pair <Node, Float> >();
    	Node current = new Node();
    	Node start = current;
    	cost_so_far.put(current, 0f);
    	frontier.add(new Pair<Node, Float>(current, 0f));
    	while(!frontier.isEmpty()){
    		current = getBest(frontier);
        	cTime = System.nanoTime();
    		
    		if(dist(current, start) >= goal || 
    		(cTime - startTime) >= (timeBudget - postSearchReserve)){
    			// Early Exit warning
				if(VERBOSE > 3){
					if((cTime - startTime) >= timeBudget - postSearchReserve){
    					System.out.println("Early Exit");
    				}
    			}
				// Extract path
    			while(current.parent() != null){
    				if(current.action() != null){
    					path.add(current);
    					current = current.parent();
    				}
    			}
    			Collections.reverse(path);
    			return path;
    		}
    		
    		for(Node next : current.genChildren()){
    			float new_cost = cost_so_far.get(current) + next.cost(current);
    			if(!cost_so_far.containsKey(next) || new_cost < cost_so_far.get(next)){
    				cost_so_far.put(next, new_cost);
    				float priority = new_cost + next.h(next._x, next._state.mario.xa);
    				frontier.add(new Pair<Node, Float>(next, priority));
    			}
    		}
    	}
    	return null;
    }
    
    @SuppressWarnings("unused")
	public ArrayList<Node> msearch(MarkovChain mc){
    	long startTime = System.nanoTime();
    	long cTime;
    	// This is used to pass states between searches
    	if (simState == null){
    		workScene = backupState();
    	}
    	else{
    		workScene = simState;
    	}
    	// Standard implementation of A* with early exit
    	// mNodes are used to deal with markov chain
    	ArrayList<Node> path = new ArrayList<Node>();
    	HashMap<Node, Float> cost_so_far = new HashMap<Node, Float>();
    	ArrayList< Pair<Node, Float> > frontier = new ArrayList<Pair <Node, Float> >();
    	Node current = new mNode(mc.estimate());
    	Node start = current;
    	cost_so_far.put(current, 0f);
    	frontier.add(new Pair<Node, Float>(current, 0f));
    	while(!frontier.isEmpty()){
    		current = getBest(frontier);
        	cTime = System.nanoTime();
    		
    		if(dist(current, start) >= goal || 
    		(cTime - startTime) >= (timeBudget - postSearchReserve)){
    			if(VERBOSE > 3){
    				// Early Exit warning
    				if((cTime - startTime) >= timeBudget - postSearchReserve){
    					System.out.println("Early Exit");
    				}
    			}
    			// Extract Path
    			while(current.parent() != null){
    				if(current.action() != null){
    					path.add(current);
    					current = current.parent();
    				}
    			}
    			Collections.reverse(path);
    			return path;
    		}
    		
    		for(Node next : current.genChildren()){
    			float new_cost = cost_so_far.get(current) + next.cost(current);
    			if(!cost_so_far.containsKey(next) || new_cost < cost_so_far.get(next)){
    				cost_so_far.put(next, new_cost);
    				float priority = new_cost + next.h(next._x, next._state.mario.xa);
    				frontier.add(new Pair<Node, Float>(next, priority));
    			}
    		}
    	}
    	return null;
    }
    
    private Node getBest(ArrayList< Pair<Node, Float> > frontier){
    	Float min = frontier.get(0).b;
    	int mini = 0;
    	for(int i = 0; i < frontier.size(); i++){
    		Float check = frontier.get(i).b;
    		if(check < min){
    			min = check;
    			mini = i;
    		}
    	}
    	Node toReturn = frontier.get(mini).a;
    	frontier.remove(mini);
    	return toReturn;
    }

    //////////////////////Simulation/////////////////////////
	public void simStep(boolean[] action){
		workScene.mario.setKeys(action);
		workScene.tick();
	}
	
	public LevelScene simulateAction(boolean[] action, int repeat){
		for(int i = 0; i < repeat; i++){
			simStep(action);
		}
		LevelScene toReturn = cloneState(workScene);
		workScene = cloneState(levelScene);
		return toReturn;
	}

	public void advanceStep(boolean[] action){
		levelScene.mario.setKeys(action);
		levelScene.tick();
	}

	public void setLevelPart(byte[][] levelPart, float[] enemies){
		levelScene.setLevelScene(levelPart);
		levelScene.setEnemies(enemies);
	}
	
    ////////////////////Utility Functions////////////////////
	// Used to reset state after a search has been completed
    public void simNull(){
    	simState = null;
    }
    
    // Distance in grid (0,22) units
    public float dist(Node n1, Node n2){
    	float diffx = n1._x - n2._x;
    	float diffy = n1._y - n2._y;
		return (float) Math.sqrt((diffx * diffx) + (diffy * diffy));
    }
	
	public LevelScene cloneState(LevelScene l){
		LevelScene toReturn = null;
		try{
			toReturn = (LevelScene) l.clone();
		}
		catch (CloneNotSupportedException e){
			e.printStackTrace();
		}
		return toReturn;
	}
	
	public void restoreState(LevelScene l){
		levelScene = l;
	}
	
	public LevelScene backupState(){
		LevelScene sceneCopy = null;
		try
		{
			sceneCopy = (LevelScene) levelScene.clone();
		} catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}
		
		return sceneCopy;
	}
    
    public String printAction(boolean[] action)
    {
    	String s = "";
    	if (action[Mario.KEY_RIGHT]) s+= "Forward ";
    	if (action[Mario.KEY_LEFT]) s+= "Backward ";
    	if (action[Mario.KEY_SPEED]) s+= "Speed ";
    	if (action[Mario.KEY_JUMP]) s+= "Jump ";
    	if (action[Mario.KEY_DOWN]) s+= "Duck";
    	return s;
    }
    
    // distance covered at maximum acceleration with initialSpeed for ticks timesteps 
    private float maxForwardMovement(float initialSpeed, int ticks){
    	float y = ticks;
    	float s0 = initialSpeed;
    	return (float) (99.17355373 * Math.pow(0.89,y+1)
    	  -9.090909091*s0*Math.pow(0.89,y+1)
    	  +10.90909091*y-88.26446282+9.090909091*s0);
    }

    /////////////////////////Node & Pair//////////////////////
	private class Node{
		private boolean[] _action = null;
		public float _x, _y;
		protected LevelScene _state;
		protected int _repeat = 1;
		private Node _parent = null;
		private int _oDist;
		//////////////////Initialization/////////////////////
		// Used only for root node
		public Node(){
			_state = backupState();
			_x = _state.mario.x / 16;
			_y = _state.mario.y / 16;
			_oDist = 0;
		}
		
		public Node(boolean[] action, LevelScene state, Node parent){
			_action = action;
			_state  = state;
			_x = _state.mario.x / 16;
			_y = _state.mario.y / 16;
			_parent = parent;
			_oDist = parent.oDist() + 1;
		}
		
		//////////////////////Search/////////////////////////
		public ArrayList<Node> genChildren(){
			if(_state == null){
				System.out.println("No state given");
				return null;
			}
			ArrayList<boolean[]> moves = genMoves();
			ArrayList<Node> children = new ArrayList<Node>();
			for(boolean[] move : moves){
				workScene = cloneState(_state);
				children.add(new Node(move, simulateAction(move, _repeat), this));
			}
			workScene = cloneState(levelScene);
			return children;
		}
		
		///////////////////////Cost//////////////////////////
		private float nDamage(){
	    	return _state.mario.damage;
	    }
		
		private float gap(){
	    	if (_state.level.isGap[(int) (_state.mario.x/16)] &&
	    	_state.mario.y >= _state.level.gapHeight[(int) (_state.mario.x/16)]*16){
	    		return 1000;
	    	}
	    	return 0;
		}
		
		// Damage times how far mario has come
		// This allows nodes to occasionally have the same cost so a tiebreaker
		// is used in the frontier's get function
		public float cost(Node n){
			int death = 1;
			if(_state.mario.status == 0){
				death = 100000;
			}
			float d  = (nDamage() - n.nDamage());
			float od = (1000000 - 100 * oDist());
			float g = gap();
			return death * (g * g) * (d * od)/( 1 + 
					(n._state.powerUpsCollected - _state.powerUpsCollected) + 
					(n._state.enemiesJumpedOn   - _state.enemiesJumpedOn)   +
					(n._state.coinsCollected    - _state.coinsCollected));
		}
		
		// Distance to the location of the goal in x
		// Optional scale might affect performance at end of game
		public float h(float currx, float currxa){
			int scale = 1000;
			float h = (100000 //- scale
			- (maxForwardMovement(currxa, 1000) + currx)) / maxSpeed;
			return h;
		}
		
		//////////////////////Utility////////////////////////
	    public boolean canJumpHigher(Node n, boolean checkParent){
	    	if (n._parent != null && checkParent
	    			&& canJumpHigher(n._parent, false))
	    			return true;
	    	return n._state.mario.mayJump() || (n._state.mario.jumpTime > 0);
	    }
	    
	    protected ArrayList<boolean[]> genMoves(){
			ArrayList<boolean[]> toReturn = new ArrayList<boolean[]>();
			if(canJumpHigher(this, true)){ 
				//Jump Right
				toReturn.add(createAction(false, true, false, true, false));
				toReturn.add(createAction(false, true, false, true, true ));
				// Jump
				toReturn.add(createAction(false, false, false, true, false));
				toReturn.add(createAction(false, false, false, true, true ));
				// Jump Left
				toReturn.add(createAction(true, false, false, true, false));
				toReturn.add(createAction(true, false, false, true, true ));
			}
			// Right
			toReturn.add(createAction(false, true, false, false, false));
			toReturn.add(createAction(false, true, false, false, true ));
			// Left
			toReturn.add(createAction(true, false, false, false, false));
			toReturn.add(createAction(true, false, false, false, true ));
			// duck
			toReturn.add(createAction(false, false, true, false, false));
			return toReturn;
	    }
	    
		private boolean[] createAction(boolean left, boolean right,
		boolean down, boolean jump, boolean speed){
	    	boolean[] action = new boolean[5];
	    	action[Mario.KEY_DOWN] = down;
	    	action[Mario.KEY_JUMP] = jump;
	    	action[Mario.KEY_LEFT] = left;
	    	action[Mario.KEY_RIGHT] = right;
	    	action[Mario.KEY_SPEED] = speed;
	    	return action;
	    }
		//////////////////////Accessor///////////////////////
		public Node parent(){
			return _parent;
		}
	
		public boolean[] action(){
			return _action;
		}
	
		public LevelScene state(){
			return _state;
		}
	
		public int oDist(){
			return _oDist;
		}
	}
	
	private class mNode extends Node{
		private float _noiseP;
		
		public mNode(boolean[] action, LevelScene state, Node parent, float noiseP){
			super(action, state, parent);
			_noiseP = noiseP;
		}
		
		public mNode(float noiseP){
			super();
			_noiseP = noiseP;
		}
		
		public float cost(Node n){
			if(_noiseP != 1){
				return (super.cost(n) / (1 - _noiseP));
			}
			return (super.cost(n) / .00001f);
		}
		
		public ArrayList<Node> genChildren(MarkovChain mc){
			if(_state == null){
				System.out.println("No state given");
				return null;
			}
			ArrayList<boolean[]> moves = genMoves();
			ArrayList<Node> children = new ArrayList<Node>();
			mc.backup();
			for(boolean[] move : moves){
				workScene = cloneState(_state);
				mc.observe(move[3]);
				children.add(new mNode(move, simulateAction(move, _repeat), this, mc.estimate()));
				mc.restore();
			}
			workScene = cloneState(levelScene);
			return children;
		}
		
	}
	
	public class Pair<A,B>{
		public A a;
		public B b;
		
		public Pair(A a, B b){
			this.a = a;
			this.b = b;
			
		}
		
	}
}