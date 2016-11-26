// Matt Grant
// This is a modification of Robin Baumgarten's original code
// All searching functionality was stripped and only simulation code was left
package competition.noisy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import ch.idsia.mario.engine.GlobalOptions;
import ch.idsia.mario.engine.sprites.Mario;
import competition.noisy.level.Level;


// Grid position is actual position / 16
// Don't ask
public class AStarSimulator{
	
    public LevelScene levelScene;
    public LevelScene workScene;
    private float maxMarioSpeed = 10.9090909f;
    public int debugPos = 0;
    
    public int timeBudget = 20; // ms 
    // max actual location is 176
    private int maxRight = 11;
    
    public ArrayList<boolean[]> getPlan(){
    	ArrayList<Node> nodes = search();
    	ArrayList<boolean[]> plan = new ArrayList<boolean[]>();
    	for(Node n : nodes){
    		for(int i = 0; i < n._repeat; i++){
    			plan.add(n._action);
    		}
    	}
    	for(boolean[] b : plan){
    		System.out.println(printAction(b));
    	}
    	return plan;
    }
    
    public ArrayList<Node> search(){
    	workScene = backupState();
    	ArrayList<Node> path = new ArrayList<Node>();
    	HashMap<Node, Float> cost_so_far = new HashMap<Node, Float>();
    	ArrayList< Pair<Node, Float> > frontier = new ArrayList<Pair <Node, Float> >();
    	Node current = new Node();
    	cost_so_far.put(current, 0f);
    	frontier.add(new Pair<Node, Float>(current, 0f));
    	while(!frontier.isEmpty()){
    		current = getBest(frontier);
    		
    		if(current._x - (levelScene.mario.x / 16) >= 8 || frontier.size() >= 20000){
    			//Extract Path
    			path.add(current);
    			while(current.parent() != null){
    				if(current.action() != null){
    					path.add(current.parent());
    					current = current.parent();
    				}
    			}
    			path.remove(current);
    			Collections.reverse(path);
    			return path;
    		}
    		for(Node next : current.genChildren()){
    			float new_cost = cost_so_far.get(current) + next.cost(current);
    			if(!cost_so_far.containsKey(next) || new_cost < cost_so_far.get(next)){
    				cost_so_far.put(next, new_cost);
    				float priority = new_cost + next.h();
    				frontier.add(new Pair<Node, Float>(next, priority));
    			}
    		}
    	}
    	System.out.println("Error in a*!");
    	return null;
    }
    
    // Roll your own priority queue because Java's has
    // has more overhead than it's worth
    public Node getBest(ArrayList< Pair<Node, Float> > frontier){
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
    

    
    public AStarSimulator()
    {
    	initialiseSimulator();
    }
    
    public float[] estimateMaximumForwardMovement(float currentAccel, boolean[] action, int ticks)
    {
    	float dist = 0;
    	float runningSpeed =  action[Mario.KEY_SPEED] ? 1.2f : 0.6f;
    	int dir = 0;
    	if (action[Mario.KEY_LEFT]) dir = -1;
    	if (action[Mario.KEY_RIGHT]) dir = 1;
    	for (int i = 0; i < ticks; i++)
    	{
    		currentAccel += runningSpeed * dir;
    		dist += currentAccel;
    		currentAccel *= 0.89f;
    	}    	
    	float[] ret = new float[2];
    	ret[0] = dist;
    	ret[1] = currentAccel;
    	return ret;
    }
    
    // distance covered at maximum acceleration with initialSpeed for ticks timesteps 
    private float maxForwardMovement(float initialSpeed, int ticks)
    {
    	float y = ticks;
    	float s0 = initialSpeed;
    	return (float) (99.17355373 * Math.pow(0.89,y+1)
    	  -9.090909091*s0*Math.pow(0.89,y+1)
    	  +10.90909091*y-88.26446282+9.090909091*s0);
    }
    
	public void setLevelPart(byte[][] levelPart, float[] enemies){
		levelScene.setLevelScene(levelPart);
		levelScene.setEnemies(enemies);
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
    
	public void initialiseSimulator()
	{
		levelScene = new LevelScene();
		levelScene.init();	
		levelScene.level = new Level(1500,15);
		workScene = backupState();
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
	
	public void advanceStep(boolean[] action)
	{
		levelScene.mario.setKeys(action);
		levelScene.tick();
	}
 
	public class Node{
		private boolean[] _action = null;
		public float _x, _y;
		private LevelScene _state;
		private int _repeat = 1;
		private Node _parent = null;
		
		// Used only for root node
		public Node(){
			_state = backupState();
			_x = _state.mario.x / 16;
			_y = _state.mario.y / 16;
		}
		
		public Node(boolean[] action, LevelScene state, Node parent){
			_action = action;
			_state  = state;
			_x = _state.mario.x / 16;
			_y = _state.mario.y / 16;
			_parent = parent;
		}
		
		public Node parent(){
			return _parent;
		}
		
		public boolean[] action(){
			return _action;
		}
		
		
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
		
		private float nDamage(){
	    	if (_state.level.isGap[(int) (_state.mario.x/16)] &&
	    	_state.mario.y > _state.level.gapHeight[(int) (_state.mario.x/16)]*16){
	    		_state.mario.damage+=5;
	    	}
	    	return _state.mario.damage;
	    }
		
		public float cost(Node n){
			float currX = n._x;
			float currY = n._y;
			// Simulated location after action
			float diffx = _x - currX;
			float diffy = _y - currY;
			if (currX < _x){
				diffx *= 2;
			} 
		    // Distance
			//float d = (float) Math.sqrt((diffx * diffx) + (diffy * diffy));
			return ((11/16) - diffx) + (nDamage()/2);
			
		}
		
	    public float h(){
	    	float relativeP = _x % 22;
	    	return 22 - relativeP;
	    }
		
	    public boolean canJumpHigher(Node n, boolean checkParent){
	    	if (n._parent != null && checkParent
	    			&& canJumpHigher(n._parent, false))
	    			return true;
	    	return n._state.mario.mayJump() || (n._state.mario.jumpTime > 0);
	    }
	    
	    private ArrayList<boolean[]> genMoves(){
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
			// Nothing
			//toReturn.add(createAction(false, false, false, false, false));
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