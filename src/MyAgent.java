package leidenuniv.symbolicai;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import leidenuniv.symbolicai.logic.KB;
import leidenuniv.symbolicai.logic.Predicate;
import leidenuniv.symbolicai.logic.Sentence;
import leidenuniv.symbolicai.logic.Term;

public class MyAgent extends Agent {
	
	private boolean isSubset(HashMap<String, Predicate> smallerMap, HashMap<String, Predicate> largerMap) {
		for (HashMap.Entry<String, Predicate> entry: smallerMap.entrySet()) {
			String key = entry.getKey();
            Predicate value = entry.getValue();
            
            // Check if the key exists in the larger map
            if (!largerMap.containsKey(key)) {
                return false;
            }

            // No need to check for the corresponding value as 
            // object will be different. However, if they output the 
            // same string (key), it should be enough.
		}
		return true;
	}
	
	
	@Override
	public KB forwardChain(KB kb) {
		//This method should perform a forward chaining on the kb given as argument, until no new facts are added to the KB.
		//It starts with an empty list of facts. When ready, it returns a new KB of ground facts (bounded).
		//The resulting KB includes all deduced predicates, actions, additions and deletions, and goals.
		//These are then processed by processFacts() (which is already implemented for you)
		//HINT: You should assume that forwardChain only allows *bound* predicates to be added to the facts list for now.
		
		boolean newFactsFound = true;
		Vector<Sentence> rules = new Vector<Sentence>();
		HashMap<String, Predicate> facts = new HashMap<String, Predicate>();
		HashMap<String, Predicate> actions = new HashMap<String, Predicate>();
		for (Sentence sentence: kb.rules()) {
			if (sentence.conditions.isEmpty()) {
				for (Predicate conclusion: sentence.conclusions) {
					if (conclusion.add) {
						actions.put(conclusion.toString(), conclusion);
						facts.put(conclusion.toString().substring(1), conclusion);
					}
					else if (conclusion.isAction()) actions.put(conclusion.toString(), conclusion);
					else facts.put(conclusion.toString(), conclusion);
				}
			}
			else {
				rules.add(sentence);
			}
		}
		while (newFactsFound) {
			HashMap<String, Predicate> newFacts = new HashMap<String, Predicate>();
			HashMap<String, Predicate> newActions = new HashMap<String, Predicate>();
			for (Sentence rule: rules) {
				Collection<HashMap<String, String>> substitutions = new Vector<HashMap<String,String>>();
				if (findAllSubstitions(substitutions, new HashMap<String,String>(), rule.conditions, facts)) {
					for (HashMap<String, String> substitution: substitutions) {
						for (Predicate conclusion: rule.conclusions) {
							Predicate conclusionSubst = new Predicate(conclusion.toString());
							conclusionSubst = substitute(conclusionSubst, substitution);
							if (conclusionSubst.add) {
								newActions.put(conclusionSubst.toString(), conclusionSubst);
								newFacts.put(conclusionSubst.toString().substring(1), conclusionSubst);
							}
							if (conclusionSubst.isAction()) newActions.put(conclusionSubst.toString(), conclusionSubst);
							else newFacts.put(conclusionSubst.toString(), conclusionSubst);
						}
					}
				}
			}
			if (isSubset(newFacts, facts) && isSubset(newActions, actions)) {
				newFactsFound = false;
			}
			else {
				facts.putAll(newFacts);
				actions.putAll(newActions);
			}
		}
		
		KB resultingKB = new KB();
		// There is no need to add rules as they are either not bound or their conclusions
		// are somewhere in the facts or actions already
//		for (Sentence rule: rules) {
//			resultingKB.add(rule);
//		}
		for (Predicate fact: facts.values()) {
			if (fact.bound()) resultingKB.add(new Sentence(fact.toString()));
		}
		for (Predicate action: actions.values()) {
			if (action.bound()) resultingKB.add(new Sentence(action.toString()));
		}
		
		return resultingKB;
	}
	
	// Function to check whether only reserved predicates are left in the conditions
	private boolean onlyReservedPredicatesLeft(Vector<Predicate> conditions) {
		// counter for the number of the reserved predicates in the vector
		int counter = 0;
		for (Predicate condition: conditions) {
			if (condition.eql || condition.not || condition.neg) {
				counter++;
			}
		}
		if (counter == conditions.size()) { // all predicates in the vector are reserved
			return true;
		}
		else { // not all predicates are reserved
			return false;
		}
	}
	
	// Function to check if the union is possible
    private boolean unionIsPossible(HashMap<String, String> map1, HashMap<String, String> map2) {
        for (HashMap.Entry<String, String> entry : map1.entrySet()) {
            String key = entry.getKey();
            String value1 = entry.getValue();
            String value2 = map2.get(key);

            // If the key is present in both maps and values are different, union is not possible
            if (value2 != null && !value1.equals(value2)) {
                return false;
            }
        }
        // Union is possible
        return true;
    }

	@Override
	public boolean findAllSubstitions(Collection<HashMap<String, String>> allSubstitutions,
			HashMap<String, String> substitution, Vector<Predicate> conditions, HashMap<String, Predicate> facts) {
		//Recursive method to find *all* valid substitutions for a vector of conditions, given a set of facts
		//The recursion is over Vector<Predicate> conditions (so this vector gets shorter and shorter, the farther you are with finding substitutions)
		//It returns true if at least one substitution is found (can be the empty substitution, if nothing needs to be substituted to unify the conditions with the facts)
		//allSubstitutions is a list of all substitutions that are found, which was passed by reference (so you use it build the list of substitutions)
		//substitution is the one we are currently building recursively.
		//conditions is the list of conditions you  still need to find a subst for (this list shrinks the further you get in the recursion).
		//facts is the list of predicates you need to match against (find substitutions so that a predicate form the conditions unifies with a fact)
		
		if (conditions.isEmpty()) { //base case: we successfully substituted for all conditions
			allSubstitutions.add(substitution);
		}
		else { // recursive case: we still have conditions to find substitutions
			Predicate condition = conditions.lastElement();
			if (condition.eql || condition.not || condition.neg) { // base case, considering reserved predicates
				boolean passed = false;
				if (onlyReservedPredicatesLeft(conditions)) {
					Predicate conditionSubst = new Predicate(condition.toString());
					conditionSubst = substitute(conditionSubst, substitution);
					if (conditionSubst.eql()) passed = true;
					else if (conditionSubst.not()) passed = true;
					else if (conditionSubst.neg) {
						if (facts.containsKey(conditionSubst.toString()) || 
								!facts.containsKey(conditionSubst.toString().substring(1))) {
							passed = true;
						}
					}
					
					if (passed) {
						Vector<Predicate> conditionsLeft = new Vector<>(conditions.subList(0, conditions.size() - 1));
						findAllSubstitions(allSubstitutions, substitution, conditionsLeft, facts);
					}
				}
				else {
					conditions.remove(conditions.size() - 1);
					conditions.insertElementAt(condition, 0);	
					findAllSubstitions(allSubstitutions, substitution, conditions, facts);
				}
			}
			else { // base case, considering not reserved predicate
				for (Predicate fact: facts.values()) {
					HashMap<String, String> unification = unifiesWith(condition, fact);
					if (unification != null && unionIsPossible(substitution, unification)) {
						HashMap<String, String> newSubstitution = new HashMap<>(substitution);
						newSubstitution.putAll(unification);
						Vector<Predicate> conditionsLeft = new Vector<>(conditions.subList(0, conditions.size() - 1));
						findAllSubstitions(allSubstitutions, newSubstitution, conditionsLeft, facts);
					}
				}
			}
		}
		
		if (!allSubstitutions.isEmpty()) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public HashMap<String, String> unifiesWith(Predicate p, Predicate f) {
		//Returns the valid substitution for which p predicate unifies with f
		//You may assume that Predicate f is fully bound (i.e., it has no variables anymore)
		//The result can be an empty substitution, if no subst is needed to unify p with f (e.g., if p an f contain the same constants or do not have any terms)
		//Please note because f is bound and p potentially contains the variables, unifiesWith is NOT symmetrical
		//So: unifiesWith("human(X)","human(joost)") returns X=joost, while unifiesWith("human(joost)","human(X)") returns null 
		//If no subst is found it returns null
		
		// Check whether there are any variables in the 2nd predicate
		for (Term term: f.getTerms()) {
			if (term.var) {
				return null;
			}
		}
		
		// Check whether the predicates have the same amount of terms
		// and whether there are the same predicates (the names are equal)
		if ((p.getTerms().size() != f.getTerms().size()) ||
				(!p.getName().equals(f.getName()))) {
			return null;
		}
		else {
			HashMap<String, String> substitution = new HashMap<String, String>();
			for (int i=0; i<p.getTerms().size(); i++) {
				Term pTerm = p.getTerm(i);
				Term fTerm = f.getTerm(i);
				
				if (pTerm.var) {
					if ((substitution.containsKey(pTerm.toString())) &&
							(substitution.get(pTerm.toString()) != fTerm.toString())) {
						return null;
					}
					else {
						substitution.put(pTerm.toString(), fTerm.toString());
					}
				}
				else {
					if (!pTerm.toString().equals(fTerm.toString())) {
						return null;
					}
				}
			}
			return substitution;
		}
	}

	@Override
	public Predicate substitute(Predicate old, HashMap<String, String> s) {
		// Substitutes all variable terms in predicate <old> for values in substitution <s>
		//(only if a key is present in s matching the variable name of course)
		//Use Term.substitute(s)
		
		for (Term term: old.getTerms()) {
			term.substitute(s);
		}
		
		return old;
	}

	@Override
	public Plan idSearch(int maxDepth, KB kb, Predicate goal) {
		//The main iterative deepening loop
		//Returns a plan, when the depthFirst call returns a plan for depth d.
		//Ends at maxDepth
		//Predicate goal is the goal predicate to find a plan for.
		//Return null if no plan is found.
		for (int i = 1; i<=maxDepth; i++) {
			Plan plan = depthFirst(i, 0, kb, goal, new Plan());
			if (plan != null) {
				return plan;
			}
		}
		return null;
	}

	@Override
	public Plan depthFirst(int maxDepth, int depth, KB state, Predicate goal, Plan partialPlan) {
		//Performs a depthFirst search for a plan to get to Predicate goal
		//Is a recursive function, with each call a deeper action in the plan, building up the partialPlan
		//Caps when maxDepth=depth
		//Returns (bubbles back through recursion) the plan when the state entails the goal predicate
		//Returns null if capped or if there are no (more) actions to perform in one node (state)
		//HINT: make use of think() and act() using the local state for the node in the search you are in.
		
		if (state.contains(goal)) { // base case, we satisfied the goal
			return partialPlan;
		}
		else if (depth == maxDepth) { // another base case: we reached the bottom
			return null;
		}
		else { // recursive case
			Agent imaginaryAgent = new MyAgent();
			
			imaginaryAgent.loadKnowledgeBase("percepts", new File("data/percepts.txt"));
			imaginaryAgent.loadKnowledgeBase("program", new File("data/program.txt"));
			imaginaryAgent.loadKnowledgeBase("actions", new File("data/actions.txt"));
			
			imaginaryAgent.believes = state;
			imaginaryAgent.desires.add(new Sentence(goal.toString()));
			imaginaryAgent.think(imaginaryAgent.believes, imaginaryAgent.desires, imaginaryAgent.intentions);
			
			for (Sentence intentionSentence: imaginaryAgent.intentions.rules()) {
				for (Predicate intention: intentionSentence.conclusions) {
					KB newState = state.union(new KB());
					imaginaryAgent.act(null, intention, newState, new KB());
					Plan newPlan = new Plan(partialPlan);
					newPlan.add(intention);
					Plan finalPlan = depthFirst(maxDepth, depth+1, newState, goal, newPlan);
					if (finalPlan != null) {
						return finalPlan;
					}
				}
			}
		}
		return null;
	}
}
