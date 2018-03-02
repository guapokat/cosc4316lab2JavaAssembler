/*
 * Virgil Martinez
 * 2 March 2018
 * COSC 4316-01
 */
package lab2;
import java.util.*;
import java.io.*;


public class Lab2 {
        public static final int HALT 	= 0;
	public static final int PUSH 	= 1;
	public static final int RVALUE 	= 2;
	public static final int LVALUE 	= 3;
	public static final int POP 	= 4;
	public static final int STO 	= 5;
	public static final int COPY 	= 6;
	public static final int ADD 	= 7;
	public static final int SUB 	= 8;
	public static final int MPY 	= 9;
	public static final int DIV 	= 10;
	public static final int MOD 	= 11;
	public static final int NEG 	= 12;
	public static final int NOT 	= 13;
	public static final int OR 	= 14;
	public static final int AND 	= 15;
	public static final int EQ 	= 16;
	public static final int NE 	= 17;
	public static final int GT 	= 18;
	public static final int GE	= 19;
	public static final int LT 	= 20;
	public static final int LE 	= 21;
	public static final int LABEL 	= 22;
	public static final int GOTO 	= 23;
	public static final int GOFALSE = 24;
	public static final int GOTRUE 	= 25;
	public static final int PRINT 	= 26;
	public static final int READ 	= 27;
	public static final int GOSUB 	= 28;
	public static final int RET 	= 29;


	public static String [] opcodes = {"HALT", "PUSH", "RVALUE", "LVALUE","POP", "STO", "COPY", "ADD", "SUB", "MPY", "DIV",
			"MOD", "NEG", "NOT", "OR", "AND", "EQ", "NE", "GT", "GE", "LT", "LE", "LABEL", "GOTO", "GOFALSE", "GOTRUE",
			"PRINT", "READ", "GOSUB", "RET"};

	public static void main(String [] args)throws IOException
	{
		// get filename
		String filename = "input.txt";
		SymbolTable symTab = new SymbolTable();

		if (args.length != 0)
		{
			filename = args[0];
		}
		else
		{
			filename = "simple.asm";
		}
                System.out.println("The filename is: " + filename);

		// Open file for input
		Scanner infile = new Scanner(new File(filename));

		// pass 1 -- build symbol table
		pass1(infile, symTab);
		infile.close();

		// pass 2 -- assemble
		// reopen source file
		infile = new Scanner(new File(filename));

		// pass 2 -- output binary code
		pass2(infile,symTab);
		infile.close();

		// print symbol table
		dumpSymbolTable(symTab);
		System.out.println("Done");
	}

	public static int lookUpOpcode(String s)
	{
		for(int i = 0; i < opcodes.length; i++)
		{
			if (s.equalsIgnoreCase(opcodes[i]))
			{
				return i;
			}
		}
		System.err.println("\nInvalid opcode:" + s);
		return -1;
	}

	public static void pass1(Scanner infile, SymbolTable tab)
	{
		// initialize location counter, etc.
		int locationCounter = 0;
		String line;
		Scanner input;
		String lexeme;

		// find start of data section
		do
		{
			line = infile.nextLine();
			System.out.println(line);
			input = new Scanner(line);
		} while (!input.next().equalsIgnoreCase("Section"));
		if (!input.next().equalsIgnoreCase(".data"))
		{
			System.err.println("Error:  Missing 'Section .data' directive");
			System.exit(1);
		}
		else
		{
			System.out.println("Parsing data section, pass 1");
		}

		// build symbol table from variable declarations
		line = infile.nextLine();
		input = new Scanner(line);

		// data section ends where code section begins
		while(!(lexeme = input.next()).equalsIgnoreCase("Section"))
		{
			// look for labels (they end with a colon)
			int pos = lexeme.indexOf(':');
			if (pos > 0)
			{
				lexeme = lexeme.substring(0,pos);
			}
			else
			{
				System.err.println("error parsing " + line);
			}
			// insert the lexeme, the type, and its address into the symbol table
			tab.insert(lexeme,"Int",locationCounter);
			locationCounter++;
			line = infile.nextLine();
			input = new Scanner(line);
		}

		// Now, parse the code section, looking for the label directive
		System.out.println("Parsing code section, pass 1");
		locationCounter=0;
		while(infile.hasNext())
		{
			line = infile.nextLine();
			input = new Scanner(line);
			lexeme = input.next();
			// when a label is found, place it and its code offset in the symbol table
			if (lexeme.equalsIgnoreCase("label"))
			{
				lexeme = input.next();
				tab.insert(lexeme,"Code",locationCounter);
			}
			locationCounter++;
		}
	}

	// generate the code
	public static void pass2(Scanner infile, SymbolTable tab)
	{
		// initialize location counter, etc.
		int locationCounter = 0;
		String line;
		Scanner input;
		String lexeme;
		int symTabPtr;
		SymbolTableEntry entry;
		final int NULL = -1;
		// find start of next section
		do
		{
			line = infile.nextLine();
			input = new Scanner(line);

		} while (!input.next().equalsIgnoreCase("Section"));
		if (!input.next().equalsIgnoreCase(".data"))
		{
			System.err.println("Error:  Missing 'Section .data' directive");
			System.exit(1);
		}
		else
		{
			System.out.println("Parsing data section, pass 2");
		}
		line = infile.nextLine();
		input = new Scanner(line);

		while(!(lexeme = input.next()).equalsIgnoreCase("Section"))
		{
			// data section has been processed in previous pass, so skip this
			line = infile.nextLine();
			input = new Scanner(line);
		}

		// Now, let's generate some code
		System.out.println("Parsing code section, pass 2");
		locationCounter=0;
                FileOutputStream fileOut = null;
                DataOutputStream dataOut = null;
                
                try{
                    fileOut = new FileOutputStream("out.bin");
                    dataOut = new DataOutputStream(fileOut);
                } catch (IOException err) {
                    err.printStackTrace();
                }
                
                
		// while not end of file keep parsing
		while(infile.hasNext())
		{
			line = infile.nextLine();
			input = new Scanner(line);
			lexeme = input.next();
			int ptr;
			//	lookup opcode and generate appropriate instructions
			int opcode = lookUpOpcode(lexeme);
			switch(opcode)
			{
				case HALT:
					insertCode(locationCounter, HALT);
                                        try {
                                            dataOut.writeShort(HALT);
                                            dataOut.writeShort(0);
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;
                                        
				case PUSH:
					lexeme = input.next();
					insertCode(locationCounter, PUSH, Integer.parseInt(lexeme));
                                        try {
                                            dataOut.writeShort(PUSH);
                                            dataOut.writeShort(Integer.parseInt(lexeme));
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;
				case RVALUE:
					lexeme = input.next();
					ptr = tab.lookup(lexeme);
					insertCode(locationCounter, RVALUE, tab.get(ptr).getAddress());
                                        try {
                                            dataOut.writeShort(RVALUE);
                                            dataOut.writeShort(tab.get(ptr).getAddress());
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
                                        
					break;
				case LVALUE:
					lexeme = input.next();
					ptr = tab.lookup(lexeme);
					insertCode(locationCounter, LVALUE, tab.get(ptr).getAddress());
                                        try {
                                            dataOut.writeShort(POP);
                                            dataOut.writeShort(tab.get(ptr).getAddress());
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;
				case POP:
					lexeme = input.next();
					ptr = tab.lookup(lexeme);
					insertCode(locationCounter, POP, tab.get(ptr).getAddress());
                                        try {
                                            dataOut.writeShort(POP);
                                            dataOut.writeShort(tab.get(ptr).getAddress());
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;	
				case STO:
					insertCode(locationCounter, STO);
                                        try {
                                            dataOut.writeShort(STO);
                                            dataOut.writeShort(0);
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;
				case ADD:
					insertCode(locationCounter, ADD);
                                        try {
                                            dataOut.writeShort(ADD);
                                            dataOut.writeShort(0);
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;
				case SUB:
					insertCode(locationCounter, SUB);
                                        try {
                                            dataOut.writeShort(SUB);
                                            dataOut.writeShort(0);
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;
				case MPY:
					insertCode(locationCounter, MPY);
                                        try {
                                            dataOut.writeShort(MPY);
                                            dataOut.writeShort(0);
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;
				case DIV:
					insertCode(locationCounter, DIV);
                                        try {
                                            dataOut.writeShort(DIV);
                                            dataOut.writeShort(0);
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;
				case MOD:
					insertCode(locationCounter, MOD);
                                        try {
                                            dataOut.writeShort(MOD);
                                            dataOut.writeShort(0);
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;
				case NEG:
					insertCode(locationCounter, NEG);
                                        try {
                                            dataOut.writeShort(NEG);
                                            dataOut.writeShort(0);
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;
				case NOT:
					insertCode(locationCounter, NOT);
                                        try {
                                            dataOut.writeShort(NOT);
                                            dataOut.writeShort(0);
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;
				case OR:
					insertCode(locationCounter, OR);
                                        try {
                                            dataOut.writeShort(OR);
                                            dataOut.writeShort(0);
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;
				case AND:
					insertCode(locationCounter, AND);
                                        try {
                                            dataOut.writeShort(AND);
                                            dataOut.writeShort(0);
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;
				case EQ:
					insertCode(locationCounter, EQ);
                                        try {
                                            dataOut.writeShort(EQ);
                                            dataOut.writeShort(0);
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;
				case NE:
					insertCode(locationCounter, NE);
                                        try {
                                            dataOut.writeShort(NE);
                                            dataOut.writeShort(0);
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;
				case GT:
					insertCode(locationCounter, GT);
                                        try {
                                            dataOut.writeShort(GT);
                                            dataOut.writeShort(0);
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;
				case GE:
					insertCode(locationCounter, GE);
                                        try {
                                            dataOut.writeShort(GE);
                                            dataOut.writeShort(0);
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;
				case LT:
					insertCode(locationCounter, LT);
                                        try {
                                            dataOut.writeShort(LT);
                                            dataOut.writeShort(0);
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;
				case LE:
					insertCode(locationCounter, LE);
                                        try {
                                            dataOut.writeShort(LE);
                                            dataOut.writeShort(0);
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;
				case LABEL:
					insertCode(locationCounter, LABEL);
                                        try {
                                            dataOut.writeShort(LABEL);
                                            dataOut.writeShort(0);
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;
				case GOTO:
					lexeme = input.next();
					ptr = tab.lookup(lexeme);
					insertCode(locationCounter, GOTO, ptr);
                                        try {
                                            dataOut.writeShort(GOTO);
                                            dataOut.writeShort(ptr);
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;
				case GOFALSE:
					lexeme = input.next();
					ptr = tab.lookup(lexeme);
					insertCode(locationCounter, GOFALSE, ptr);
                                        try {
                                            dataOut.writeShort(GOFALSE);
                                            dataOut.writeShort(ptr);
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;
				case GOTRUE:
					lexeme = input.next();
					ptr = tab.lookup(lexeme);
					insertCode(locationCounter, GOTRUE, ptr);
                                        try {
                                            dataOut.writeShort(GOTRUE);
                                            dataOut.writeShort(ptr);
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;
				case PRINT:
					insertCode(locationCounter, PRINT);
                                        try {
                                            dataOut.writeShort(PRINT);
                                            dataOut.writeShort(0);
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;	
				case READ:
					insertCode(locationCounter, READ);
                                        try {
                                            dataOut.writeShort(READ);
                                            dataOut.writeShort(0);
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;	
				case GOSUB:
					lexeme = input.next();
					ptr = tab.lookup(lexeme);
					insertCode(locationCounter, LVALUE, tab.get(ptr).getAddress());
                                        try {
                                            dataOut.writeShort(LVALUE);
                                            dataOut.writeShort(tab.get(ptr).getAddress());
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;	
				case RET:
					insertCode(locationCounter, RET);
                                        try {
                                            dataOut.writeShort(RET);
                                            dataOut.writeShort(0);
                                        } catch(IOException err) {
                                            err.printStackTrace();
                                        }
					break;
				default:
					System.err.println("Unimplemented opcode:  " + opcode);
					System.exit(opcode);
			}
			locationCounter++;
		}
                try {
                    fileOut.close();
                } catch(IOException err) {
                    err.printStackTrace();
                }
	}

	public static void insertCode(int loc, int opcode, int operand)
	{
		System.out.println(loc + ":\t" + opcode + "\t" + operand);;
	}

	public static void insertCode(int loc, int opcode)
	{
		insertCode(loc,opcode,0);
	}

	public static void dumpSymbolTable(SymbolTable tab)
	{
		System.out.println("\nlexeme \ttype \taddress");
		System.out.println("-----------------------------------------");
		for(int i=0; i<tab.size(); i++)
		{
			System.out.println(tab.get(i));
		}
	}
 
        public static class SymbolTable{
            //tab.insert(lexeme,"Int",locationCounter);
            
            ArrayList<SymbolTableEntry> instructions = new ArrayList<SymbolTableEntry>();
            SymbolTableEntry temp = new SymbolTableEntry();
            
            public void insert(String opcode, String type, int counter){
                temp.opcode = opcode;
                temp.type = type;
                temp.count = counter;
                instructions.add(temp);
            }
            
            public int lookup(String opcode){
                for(int i = 0; i < instructions.size(); i++){
                    if(instructions.get(i).opcode.equals(opcode)){
                        return instructions.get(i).count;
                    }
                }
                return 0;
            }
            
            public SymbolTableEntry get(int pointer){
                return instructions.get(pointer);
            }
            
            public int size(){
                return instructions.size();
            }
        }
        
        public static class SymbolTableEntry{
            private int count;
            private String opcode;
            private String type;
                        
            public int getAddress(){
                return count;
            }
                
        }
}
