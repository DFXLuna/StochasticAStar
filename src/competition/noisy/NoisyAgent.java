package competition.noisy;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import ch.idsia.ai.agents.Agent;
import competition.noisy.AStarSimulator.*;
import java.util.ArrayList;
/**
 * User: Matt Grant 
 * Date: 11/5/2016
 * Package: competition.noisy
 */
// Some people write disgusting code. We call those people Java Devs
// I thought python was atrocious but Java is clearly on another level
// Have you ever looked at the interface for 
// implementing a Priority queue in Java? It's disgusting


public class NoisyAgent implements Agent{

    protected String name = "NoisyAgent";
    private AStarSimulator sim;
    private ArrayList<boolean[]> plan = new ArrayList<boolean[]>();
    public boolean requirePlan = true;
    private boolean[] prevAction = null;
    private int repeat = 4;
    

    public void reset(){
    	//boolean action[] = {false, true, false, true, true};
        //plan.add(action);
        requirePlan = true;
        sim = new AStarSimulator();
    }

	// TODO
    // Prioritizing going right
    // getting >1 frame jumps
    // Add noise
	// Check for inaccuracy in simulation
    // Possibly budget time
    public boolean[] getAction(Environment observation){
    	byte[][] scene = observation.getLevelSceneObservationZ(0);
    	float[] enemies = observation.getEnemiesFloatPos();
    	// Advance the sim based on last action.
    	float[] m = observation.getMarioFloatPos();
    	if(prevAction != null){
    		//sim.advanceStep(prevAction);
    		sim.levelScene.mario.x = m[0];
    		sim.levelScene.mario.y = m[1];
    	}
    	sim.setLevelPart(scene, enemies);
    	
    	// We only want to replan if the last plan has expired

    	if (sim.levelScene.mario.x != m[0] || sim.levelScene.mario.y != m[1]){
    		System.out.println("Inaccuracy in simulator position");
    	}
    	if(requirePlan){
    		//reset();
    		plan();
    		requirePlan = false;
    	}
        return popAction();
    }
    private void plan(){
    	plan.clear();
    	for(int i = 0; i < repeat; i++){
    		plan.addAll(sim.getPlan());
    	}
    	sim.simNull();
    }
    
    public boolean[] popAction(){
    	if(plan.size() == 1){
    		requirePlan = true;
    	}
		boolean[] toReturn = plan.get(0);
		prevAction = toReturn;
		plan.remove(0);
		return toReturn;
    }
    
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
