package Agents;
import jade.core.AID;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


class Preference extends JFrame {	
private AgentPrefer myAgent; 
  //  private Agent1 myAgent; 	
    Preference(AgentPrefer a) {
        super(a.getLocalName());
        myAgent = a;
    

    };	
}