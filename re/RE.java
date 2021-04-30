package re;

import fa.nfa.NFA;
import fa.State;
import fa.nfa.NFAState;

import java.util.*;

public class RE implements REInterface 
{
    int incrementState = 0;
    String regEx;

    public NFA getNFA() 
    {
        return regEx();
    }

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
    
    public RE(String regEx)
    {
        this.regEx = regEx;
    }

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

    //If when combining NFA's a problem occurs, change order of addNFAStates, etc.
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

    private NFA star(NFA base)
    {
        return null;
    }

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

    //incrementState may need to be in parenthesis of "new" statements
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

    private char peek()
    {
        return regEx.charAt(0);
    }

    private char next()
    {
        char c = peek();
        eat(c);
        return c;
    }

    // private boolean more()
    // {
    //     return regEx.length() > 0;
    // }
}