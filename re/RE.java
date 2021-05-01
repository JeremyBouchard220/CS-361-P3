package re;

import fa.nfa.NFA;
import fa.State;
import fa.nfa.NFAState;

import java.util.*;
/**
 * @author Sam Jackson, Jeremy Bouchard
 * 
 * Parses a regular expression into an NFA
 */
public class RE implements REInterface 
{
    int incrementState = 0;
    String regEx;

    /**
     * @return an NFA based on the regular expression
     */
    public NFA getNFA() 
    {
        return regEx();
    }

    /**
     * 
     * @return either the union between a term and another
     * regex or simply a term
     */
    private NFA regEx()
    {
        NFA term = term();

        if (regEx.length() > 0 && peek() == '|')
        {
            eat('|');
            NFA regEx = regEx();
            return  union(term, regEx);
        }

        else
        {
            return term;
        }
    }
    
    /**
     * 
     * @param regEx holds the regEx string
     */
    public RE(String regEx)
    {
        this.regEx = regEx;
    }

    /**
     * 
     * @return either one factor or several
     */
    private NFA term()
    {
        NFA beginningFactor = new NFA();

        while (regEx.length() > 0 && peek() != ')' && peek() != '|')
        {
            NFA newFactor = factor();

            if (!beginningFactor.getStates().isEmpty())
            {
                beginningFactor = concat(beginningFactor, newFactor);
            }

            else
            {
                beginningFactor = newFactor;
            }
        }

        return beginningFactor;
    }

    /**
     * 
     * @param nfa1
     * @param nfa2
     * @return the union of two nfas
     */
    private NFA union(NFA nfa1, NFA nfa2)
    {
        NFA result = new NFA();

        String beginningState = String.valueOf(incrementState++);
        result.addStartState(beginningState);

        result.addNFAStates(nfa1.getStates());
        result.addTransition(beginningState, 'e', nfa1.getStartState().getName());
        result.addAbc(nfa1.getABC());

        result.addNFAStates(nfa2.getStates());
        result.addTransition(beginningState, 'e', nfa2.getStartState().getName());
        result.addAbc(nfa2.getABC());

        return result;
    }

    /**
     * 
     * @return either a base, or a base star
     */
    private NFA factor()
    {
        NFA base = base();

        while (regEx.length() > 0 && peek() == '*')
        {
            eat('*');
            base = star(base);
        }

        return base;
    }

    /**
     * 
     * @param base
     * @return the star of an NFA
     */
    private NFA star(NFA base)
    {
        NFAState start = new NFAState(String.valueOf(incrementState));
        incrementState++;
        NFAState end = new NFAState(String.valueOf(incrementState));
        incrementState++;

        NFA returnState = new NFA(); //NFA to be returned at the end

        returnState.addNFAStates(base.getStates()); //start with the base
        returnState.addStartState(start.getName());
        returnState.addFinalState(end.getName());

        //if there are zero iterations:
        returnState.addTransition(start.getName(), 'e', end.getName());
        returnState.addTransition(end.getName(),'e', base.getStartState().getName());
        returnState.addTransition(start.getName(), 'e', base.getStartState().getName());
        
        //old alphabet
        returnState.addAbc(base.getABC());

        Iterator<State> it = base.getFinalStates().iterator();
        while(it.hasNext()){
            State st = it.next();
            returnState.addTransition(st.getName(), 'e', end.getName());
            Iterator<State> it2 = returnState.getFinalStates().iterator();
            while(it2.hasNext()){
                State st2 = it2.next(); //may need to make this a regular state
                if (st2.getName().equals(st.getName()))
                    ((NFAState) st2).setNonFinal();
            }
        }
        return returnState;
    }

    /**
     * 
     * @param nfa1
     * @param nfa2
     * @return two concatenated nfas
     */
    private NFA concat(NFA nfa1, NFA nfa2)
    {
        Set<State> nfa1FinalStates = nfa1.getFinalStates();
        nfa1.addNFAStates(nfa2.getStates());
        nfa1.addAbc(nfa2.getABC());

        Iterator<State> itr = nfa1FinalStates.iterator();

        while(itr.hasNext())
        {
            State state = itr.next();
            ((NFAState) state).setNonFinal();
            nfa1.addTransition(state.getName(), 'e', nfa2.getStartState().getName());
        }

        return nfa1;
    }

    /**
     * 
     * @return looks into parentheses for regex
     */
    private NFA base()
    {
        switch (peek())
        {
            case '(':
                eat('(');
                NFA reg = regEx();
                eat(')');
                return reg;
            default:
                return symbol(next());
        }
    }

    /**
     * 
     * @param symbol
     * @return an NFA based on the symbol
     */
    private NFA symbol(char symbol)
    {
        NFA newNfa = new NFA();

        NFAState startState = new NFAState(String.valueOf(incrementState));
        incrementState++;

        NFAState endState = new NFAState(String.valueOf(incrementState));
        incrementState++;

        newNfa.addStartState(startState.getName());
        newNfa.addFinalState(endState.getName());
        newNfa.addTransition(startState.getName(), symbol, endState.getName());

        Set<Character> alphabet = new LinkedHashSet<Character>();
        alphabet.add(symbol);
        newNfa.addAbc(alphabet);

        return newNfa;
    }

    /**
     * processes next char
     */
    private void eat(char c)
    {
        if (peek()== c)
        {
            this.regEx = this.regEx.substring(1);
        }
        
        else
        {
            throw new RuntimeException("Received: " + peek() + "\n" + "Expected: " + c);
        }
    }

    /**
     * 
     * @return the first char in regex
     */
    private char peek()
    {
        return regEx.charAt(0);
    }

    /**
     * 
     * @return the next char in the regex
     */
    private char next()
    {
        char c = peek();
        eat(c);
        return c;
    }
}