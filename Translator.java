import java.io.*;

public class Translator {
    private Lexer lex;
    private BufferedReader pbr;
    private Token look;
    
    SymbolTable st = new SymbolTable();
    CodeGenerator code = new CodeGenerator();
    int count=0;
    int counter = -1;

    public Translator(Lexer l, BufferedReader br) {
        lex = l;
        pbr = br;
        move();
    }

    void move() { 
        look = lex.lexical_scan(pbr);
        System.err.println("token = " + look);
    }

    void error(String s) { 
        System.out.println(s + " Metodo: " + Parser.s);
		System.exit(-1);
    }

    void match(int t) {
        Parser.s = "<match>";
		if (look.tag == t) {
			if (look.tag != Tag.EOF) move();
		} else {
			error("Errore sintattico.");
		}
    }

    public void prog() {        
        Parser.s = "<prog>";
            if( look.tag == Tag.ASSIGN || look.tag == Tag.PRINT || look.tag == Tag.READ || look.tag == Tag.WHILE 
                || look.tag == Tag.IF || look.tag == '{' ){
                    int lnext_prog = code.newLabel();
                    statlist(lnext_prog);
                    code.emitLabel(lnext_prog);
                    match(Tag.EOF);
                try {
        	        code.toJasmin();
                    }
                    catch(java.io.IOException e) {
        	            System.out.println("IO error\n");
                    };
            } else error("Errore sintattico.");
    }     

    private void statlist(int lnext_statlist) {
		Parser.s = "<statlist>";
		if( look.tag == Tag.ASSIGN || look.tag == Tag.PRINT || look.tag == Tag.READ || look.tag == Tag.WHILE 
			|| look.tag == Tag.IF || look.tag == '{'){	/*insieme guida <statlist>*/
            int l = code.newLabel();
			stat(l);
            code.emitLabel(l);
			statlistp(lnext_statlist);
		} else error("Errore sintattico.");
    } //statlist

    private void statlistp(int lnext_statlistp) {
		Parser.s = "<statlistp>";
		if(look.tag == ';' || look.tag == Tag.EOF || look.tag == '}' ){
            int stat_next = code.newLabel();
			switch (look.tag){
				case ';':
					match(';');
					stat(stat_next);
					code.emitLabel(stat_next);
					statlistp(lnext_statlistp);
					break;
				case Tag.EOF:
					break;
				default:
					break;
			}		
		} else error("Errore sintattico.");
    }

    public void stat(int S_next) {
        Parser.s = "<stat>";
		if(look.tag == Tag.ASSIGN || look.tag == Tag.PRINT || look.tag == Tag.READ || look.tag == Tag.WHILE
			|| look.tag ==Tag.IF || look.tag == '{'  ){
				switch(look.tag){
					case Tag.ASSIGN:
						match(Tag.ASSIGN);
                        int read_id_addr = st.lookupAddress(((Word)look).lexeme);
							if (read_id_addr==-1) {
								read_id_addr = count;
								st.insert(((Word)look).lexeme,count++);
							}
						expr();
                        code.emit(OpCode.istore, read_id_addr);
						match(Tag.TO);
						idlist();
						break;
					case Tag.PRINT:
						match(Tag.PRINT);
						match('(');
						exprlist();
                        code.emit(OpCode.invokestatic, 1);
						match(')');
						break;
					case Tag.READ:
						match(Tag.READ);
						match('(');
						idlist();
						match(')');
						break;
					case Tag.WHILE:
                        int S1next = code.newLabel();		//S1.next = newlabel
                        code.emitLabel(S1next);	
						match(Tag.WHILE);
						match('(');
                        int B_true = code.newLabel();		//B.true = newlabel()					
						int B_false = S_next;	
						bexpr(B_true, B_false);
						match(')');
                        code.emitLabel(B_true);				//B.true:S1.code
						stat(S_next);
						code.emit(OpCode.GOto, S1next);
						break;
					case Tag.IF:
						match(Tag.IF);
						match('(');
                        int B_true1 = code.newLabel();							
						int B_false1 = code.newLabel();
						bexpr(B_true1, B_false1);
                        match(')');
                        code.emit(OpCode.GOto, S_next);/*goto s1.next*/							
						code.emitLabel(B_false1);/*B.false: S2.code*/
						stat(S_next);	
						statp(B_true1, B_false1);
						break;
					case '{':
						match('{');
						statlist(S_next);
						match('}');
						break;
					default:
						break;
				}
	
			} else error ("Errore sintattico.");
    }

    private void statp(int S_true, int S_false) {
		Parser.s = "<statp>";
		if(look.tag == Tag.ELSE || look.tag == Tag.END){
            int stat_next = code.newLabel();
			switch(look.tag){
				case Tag.ELSE:
					match(Tag.ELSE);
					stat(stat_next);
					match(Tag.END);
					break;
				case Tag.END:
                    int S1end = code.newLabel();
					match(Tag.END);
                    code.emit(OpCode.GOto, S1end);
					break;
				default:
					break;
			}
		} else error("Errore sintattico.");
	} //statp

    private void idlist() {
        switch(look.tag) {
	    case Tag.ID:
        	int id_addr = st.lookupAddress(((Word)look).lexeme);
                if (id_addr==-1) {
                    id_addr = count;
                    st.insert(((Word)look).lexeme,count++);
                }
                match(Tag.ID);
                code.emit(OpCode.invokestatic,0);
                code.emit(OpCode.istore,id_addr);
                idlistp();
                break;
        default:
                break;
    	}
    }

    private void idlistp() {
		Parser.s = "<idlistp>";
		if( look.tag == ','){
			match(',');
			match(Tag.ID);
			idlistp();
		} else if( look.tag == Tag.EOF || look.tag == '}' || look.tag == ';' || look.tag == Tag.ELSE 
			      || look.tag == Tag.END || look.tag == ')'){
					  match(Tag.EOF);
				} else error("Errore sintattico.");
	} //idlistp

    private void bexpr(int B_true_bexpr, int B_false_bexpr) {
		Parser.s = "<bexpr>";
		if( look.tag == Tag.RELOP){
			if(look == Word.lt){
                match(Tag.RELOP);
                                expr();
                                expr();
                                code.emit(OpCode.if_icmplt, B_true_bexpr);	 
                                code.emit(OpCode.GOto,B_false_bexpr);
            }else if (look == Word.gt){
                match(Tag.RELOP);
                                expr();
                                expr();
                                code.emit(OpCode.if_icmpgt, B_true_bexpr);  
                                code.emit(OpCode.GOto,B_false_bexpr);
                
            }else if(look == Word.eq){
                match(Tag.RELOP);
                                expr();
                                expr();
                                code.emit(OpCode.if_icmpeq, B_true_bexpr);  
                                code.emit(OpCode.GOto,B_false_bexpr);
                
                
            }else if(look == Word.le){
                match(Tag.RELOP);
                                expr();
                                expr();
                                code.emit(OpCode.if_icmple, B_true_bexpr);	 
                                code.emit(OpCode.GOto,B_false_bexpr);
            }else if (look == Word.ne){ 
                match(Tag.RELOP);
                                expr();
                                expr();
                                code.emit(OpCode.if_icmpne, B_true_bexpr);	
                                code.emit(OpCode.GOto,B_false_bexpr);
            }else if (look == Word.ge){
                match(Tag.RELOP);
                                expr();
                                expr();
                                code.emit(OpCode.if_icmpge, B_true_bexpr); 
                                code.emit(OpCode.GOto,B_false_bexpr);
                
            }
		} else error("Errore sintattico.");
	} //bexpr


    private void expr() {
		Parser.s = "<expr>";
		if( look.tag == '+'){
			match('+');
			match('(');
			exprlist();
			match(')');
			while(counter>0){
			code.emit(OpCode.iadd);
			counter--;}				
		} else if( look.tag == '*'){
			match('*');
			match('(');
			exprlist();
			match(')');
			while(counter>0){
			code.emit(OpCode.imul);
			counter--;}
		} else if( look.tag == '-'){
            match('-');
            expr();
            expr();
            code.emit(OpCode.isub);
            counter--;
		} else if( look.tag == '/'){
			match('/');
		    expr();
			expr();
			code.emit(OpCode.idiv);
			counter--;
		} else if( look.tag == Tag.ID){
            int read_id_addr = st.lookupAddress(((Word)look).lexeme);
            match(Tag.ID);
            if (read_id_addr==-1) {
                  error("Errore in caricamento");
            }  
            code.emit(OpCode.iload, read_id_addr);
		} else if( look.tag == Tag.NUM){
			counter++;
            int read_num_addr = st.lookupAddress(((Word)look).lexeme);
			code.emit(OpCode.ldc, read_num_addr);
			match(Tag.NUM);
		} else error("Errore sintattico.");	
    } //expr

    private void exprlist() {
		Parser.s = "<exprlist>";
		if( look.tag == '+' || look.tag == '-'|| look.tag == '*' || look.tag == '/'
			|| look.tag == Tag.NUM || look.tag == Tag.ID){
				expr();
				exprlistp();
			} else error("Errore sintattico.");
	} //exprlist

    private void exprlistp() {
		Parser.s = "<exprlistp>";
		if( look.tag == ','){
		match(',');
		expr();
		exprlistp();
		} else if( look.tag == ')'){
			match(Tag.EOF);
		} else error("Errore sintattico.");
	} //exprlistp


    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "C:/Users/lucar/OneDrive/Desktop/UNI/LFT/lft lab 21_22/5.1/input.lft "; // il percorso del file da leggere
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Translator translator = new Translator(lex, br);
            translator.prog();
            br.close();
        } catch (IOException e) {e.printStackTrace();}
    }


}
