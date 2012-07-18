package com.conref.refactoring.splitlock.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Statement;

import soot.Unit;

public class JavaStmtChunk {
	public Statement stmt;
	public int startLine;
	public int endLine;	
	public List<Unit> instructions;
	
	public boolean isEscaped;
	
	public JavaStmtChunk(Statement stmt, int startLine, int endLine){
		this.stmt = stmt;
		this.startLine = startLine;
		this.endLine = endLine;
		this.instructions = new ArrayList<Unit>();
	}
	
	public String toString(){
		return ""+startLine+"-"+endLine +": " +stmt;
	}
	
	
	public static int findFirstEscapeStmtIndex(JavaStmtChunk[] stmts){
		int length = stmts.length;
		int firstEscape = -1; 
		
		for(int i=0; i<length; i++){
			JavaStmtChunk s = stmts[i];
			if(s.isEscaped){
				firstEscape = i;
				break;
			}
		}
		
		return firstEscape;
	}
	
	public static int findLastEscapeStmtIndex(JavaStmtChunk[] stmts){
		int length = stmts.length;	 
		int lastEscape = -1; 
		
		for(int i=length-1; i>=0; i--){
			JavaStmtChunk s = stmts[i];
			if(s.isEscaped){
				lastEscape = i;
				break;
			}
		}
		
		return lastEscape;
	}
}
