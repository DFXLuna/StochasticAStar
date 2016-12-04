package competition.noisy;

import ch.idsia.mario.environments.Environment;
import ch.idsia.ai.agents.Agent;
import java.util.ArrayList;
import java.util.Random;
/**
 * User: Matt Grant 
 * Date: 5 Noc 2016
 * Package: competition.noisy
 */
// Some people write disgusting code. We call those people Java Devs
// I thought python was atrocious but Java is clearly on another level
// Have you ever looked at the interface for 
// implementing a Priority queue in Java? It's disgusting.


// Verbosity is assigned in competition.noisy.Verbose interface
public class NoisyAgent implements Agent, Verbose{

	protected boolean action[] = new boolean[Environment.numberOfButtons];
    protected String name = "NoisyAgent";
    private AStarSimulator sim;
    private ArrayList<boolean[]> plan = new ArrayList<boolean[]>();
    public boolean requirePlan = true;
    private boolean[] prevAction = null;
    ///////////////Noise Vars///////////////
	Random rand = new Random();
    // 0 = No noise
    // 1 = Cancellation
    // 2 = Randomization
    // 3 = Markovian implementation of either 1 or 2
    private static int NOISE = 3;
    // Prob is 100 - (Percent chance for noise to occur)
    // Only matters for cancellation and randomization
    private static int PROB  = 100 - 5; 
    // These must be set to use Markovian Noise
    // observe() must also be defined.
    // An optional prior can be defined, otherwise {.5,.5} is used
	float[] trans = { 0.1f, 0.01f};
	float[] sense = { 0.3f,  0.1f};
	boolean nPrev = false;
	MarkovChain mc = null;
    ////////////////////////////////////////
    
    public void reset(){
        requirePlan = true;
        action = new boolean[Environment.numberOfButtons];
        if (NOISE > 2){
        	mc = new MarkovChain(sense, trans);
        }
        sim = new AStarSimulator();
    }
    // Interestingly, the program has a memory leak somewhere.
    // Simulation becomes inaccurate when we take more than some amount of time to plan
    
	// TODO
    // test mSearch
    // Reactive noise strategy
    // >> Entry on readme about reactive noise strat approaching always replan
    // Do stats
    // Update README.MD to link to specific lines of code
    // Fix site
    @SuppressWarnings("unused")
	public boolean[] getAction(Environment observation){
    	byte[][] scene = observation.getLevelSceneObservationZ(0);
    	float[] enemies = observation.getEnemiesFloatPos();
    	if(prevAction != null){
    		sim.advanceStep(prevAction);
    		if(NOISE > 2){
    			mc.observe(observe(prevAction));
    		}
    	}
    	sim.setLevelPart(scene, enemies);
    	
    	float[] m = observation.getMarioFloatPos();
    	if (sim.levelScene.mario.x - m[0] > 0.1f || 
    	sim.levelScene.mario.y - m[1] > 0.1f ){
    		if(VERBOSE == 1 || VERBOSE == 4){
    			System.out.println("Inaccuracy in simulator position");
    			System.out.println("Expected XY: " + sim.levelScene.mario.x + " " + sim.levelScene.mario.y);
    			System.out.println("Actual XY:   " + m[0] + " " + m[1]);
    		}
    		sim.levelScene.mario.x = m[0];
    		sim.levelScene.mario.y = m[1];
    	}
    	if(mc != null && (VERBOSE == 2 || VERBOSE == 4)){
    		mc.print();
    	}
    	// We only want to replan if the last plan has expired
    	if(requirePlan){
    		plan();
    		requirePlan = false;
    	}
    	
        return popAction();
    }
    
    //////////////////////Search/////////////////////////    
    private void plan(){
    	plan.clear();
    	plan.addAll(sim.getPlan(mc));
    	sim.simNull();
    }
    
    private boolean[] popAction(){
    	if(plan.size() == 1){
    		requirePlan = true;
    	}
		boolean[] toReturn = plan.get(0);
		plan.remove(0);
		if(NOISE > 0){
			toReturn = noise(toReturn);
		}
		prevAction = toReturn;
		return toReturn;
    }
    
    //////////////////////Noise Gen//////////////////////   
    private boolean[] noise(boolean[] action){
    	int n = rand.nextInt(101) + 1;
    	boolean[] toReturn = action;
		if(NOISE == 1 && n > PROB){
			// Cancellation
			toReturn = cancel(action);
			nPrev = true;
		}
		else if(NOISE == 2 && n > PROB){
			// Randomization
			toReturn = randomize();
			nPrev = true;
		}
    	else if(NOISE > 2){
    			// Markovian noise
    			float mProb = mNoise(action);
    			if (n > 100 - (100 * mProb)){
    				// Could use either cancel or randomize here
    				toReturn = cancel(action);
    				nPrev = true;
    			}
    			else{
    				nPrev = false;
    			}
    	}
    	else{
    		nPrev = false;
    	}
		return toReturn;
    }
    
    private float mNoise(boolean[] action){
    	// Prob is [Tt, St; Tt, Sf; Tf, St; Tf, Sf]
    	float[] probMatx = {trans[0] * sense[0], trans[0] * sense[1],
    						trans[1] * sense[0], trans[1] * sense[1]};
    	
    	if(observe(action)){
    		if(nPrev){
    			return probMatx[0];
    		}
    		else{
    			return probMatx[2];
    		}
    	}
    	else{
    		if(nPrev){
    			return probMatx[1];
    		}
    		else{
    			return probMatx[3];
    		}
    	}	
    }
    
    private boolean[] cancel(boolean[] action){
    	boolean[] toReturn = {false, false, false, false, false};
    	return toReturn;
    }
    
    private boolean[] randomize(){
    	boolean[] toReturn = new boolean[5];
		for(int i = 0; i < 5; i++){
			int r = rand.nextInt(2);
			if(r == 1){
				toReturn[i] = true;
			}
			else{
				toReturn[i] = false;
			}
		}
		return action;
    }
    
    // This observation just depends on whether or not
    // mario is currently jumping
    private boolean observe(boolean[] action){
    	if(action[3] == true){
    		return true;
    	}
    	return false;
    }
    
    //////////////////////Accessor///////////////////////
    public AGENT_TYPE getType() {
        return AGENT_TYPE.AI;
    }

    public String getName() {        
    	return name;
    }

    public void setName(String Name) {
    	this.name = Name;    
    }
    

}
