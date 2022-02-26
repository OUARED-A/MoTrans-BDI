package Agents;
import jade.core.AID;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


class Extracteur extends JFrame {	
static  boolean drap=true; 
private AgentExtrac myAgent1; 	
    Extracteur(AgentExtrac a1) {
        super(a1.getLocalName());
        myAgent1 = a1;
    

    };	
}