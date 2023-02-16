import java.io.*; 
import java.util.*;


public class Parser {
    private Lexer lex;
    private BufferedReader pbr;
    private Token look;
	public static String s;

	
    public Parser(Lexer l, BufferedReader br) { //costruttore
        lex = l;
        pbr = br;
        move(); 
    } //Parser

    void move() { /* legge il prossimo token e lo stampa */
        look = lex.lexical_scan(pbr);
        System.err.println("token = " + look);
    } //move
	 
	
    void error(String s) { /* gestione dell'errore */
		System.out.println(s + " Metodo: " + Parser.s);
		System.exit(-1);
    } //error/* 


    void match(int t) { /* verifica di non essere arrivati alla fine del file e che non ci siano errori di tipo sintattico */
		Parser.s = "<match>";
		if (look.tag == t) {
			if (look.tag != Tag.EOF) move();
		} else {
			error("Errore sintattico.");
		}
    } //match

    public void prog() { /* metodo d'inizio */
	    Parser.s = "<prog>";
		if( look.tag == Tag.ASSIGN || look.tag == Tag.PRINT || look.tag == Tag.READ || look.tag == Tag.WHILE 
			|| look.tag == Tag.IF || look.tag == '{' ){ /*insieme guida <prog>*/
			statlist();
			match(Tag.EOF);
		} else error("Errore sintattico.");
    } //start

	private void statlist() {
		Parser.s = "<statlist>";
		if( look.tag == Tag.ASSIGN || look.tag == Tag.PRINT || look.tag == Tag.READ || look.tag == Tag.WHILE 
			|| look.tag == Tag.IF || look.tag == '{'){	/*insieme guida <statlist>*/
			stat();
			statlistp();
		} else error("Errore sintattico.");
    } //statlist

	private void statlistp() {
		Parser.s = "<statlistp>";
		if(look.tag == ';' || look.tag == Tag.EOF || look.tag == '}' ){
			switch (look.tag){
				case ';':
					match(';');
					stat();
					statlistp();
					break;
				case Tag.EOF:
					break;
				default:
					break;
			}		
		} else error("Errore sintattico.");
		/*Parser.s = "<statlistp>";
		if( look.tag == ';'){ /*insieme guida
			match(';');
			stat();
			statlistp();
		} else if( look.tag == Tag.EOF || look.tag == '}'){ /*insieme guida
			match(Tag.EOF);
		} else error("Errore sintattico.");*/
	} //statlistp

	private void stat() {
		Parser.s = "<stat>";
		if(look.tag == Tag.ASSIGN || look.tag == Tag.PRINT || look.tag == Tag.READ || look.tag == Tag.WHILE
			|| look.tag ==Tag.IF || look.tag == '{'  ){
				switch(look.tag){
					case Tag.ASSIGN:
						match(Tag.ASSIGN);
						expr();
						match(Tag.TO);
						idlist();
						break;
					case Tag.PRINT:
						match(Tag.PRINT);
						match('(');
						exprlist();
						match(')');
						break;
					case Tag.READ:
						match(Tag.READ);
						match('(');
						idlist();
						match(')');
						break;
					case Tag.WHILE:
						match(Tag.WHILE);
						match('(');
						bexpr();
						match(')');
						stat();
						break;
					case Tag.IF:
						match(Tag.IF);
						match('(');
						bexpr();
						match(')');
						stat();	
						statp();
						break;
					case '{':
						match('{');
						statlist();
						match('}');
						break;
					default:
						break;
				}
	
			} else error ("Errore sintattico.");
		/*Parser.s = "<stat>";
		if( look.tag == Tag.ASSIGN){
			match(Tag.ASSIGN);
			expr();
			match(Tag.TO);
			idlist();
		} else if( look.tag == Tag.PRINT){
			match(Tag.PRINT);
			match('(');
			exprlist();
			match(')');
		} else if( look.tag == Tag.READ){
			match(Tag.READ);
			match('(');
			idlist();
			match(')');
		} else if( look.tag == Tag.WHILE){
			match(Tag.WHILE);
			match('(');
			bexpr();
			match(')');
			stat();
		} else if( look.tag ==Tag.IF){
			match(Tag.IF);
			match('(');
			bexpr();
			match(')');
			stat();	
			statp();
		} else if( look.tag == '{'){
			match('{');
			statlist();
			match('}');
		} else error("Errore sintattico."); */
	} //stat

	private void statp() {
		Parser.s = "<statp>";
		if(look.tag == Tag.ELSE || look.tag == Tag.END){
			switch(look.tag){
				case Tag.ELSE:
					match(Tag.ELSE);
					stat();
					match(Tag.END);
					break;
				case Tag.END:
					match(Tag.END);
					break;
				default:
					break;
			}
		} else error("Errore sintattico.");
	} //statp

	private void idlist() {
		Parser.s = "<idlist>";
		if( look.tag == Tag.ID){
			match(Tag.ID);
			idlistp();
		} else error("Errore sintattico."); 
	} //idlist

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

	private void bexpr() {
		Parser.s = "<bexpr>";
		if( look.tag == Tag.RELOP){
			match(Tag.RELOP);
			expr();
			expr();
		} else error("Errore sintattico.");
	} //bexpr
	
	private void expr() {
		Parser.s = "<expr>";
		if( look.tag == '+'){
			match('+');
			match('(');
			exprlist();
			match(')');
		} else if( look.tag == '*'){
			match('*');
			match('(');
			exprlist();
			match(')');
		} else if( look.tag == '-'){
			match('-');
			expr();
			expr();
		} else if( look.tag == '/'){
			match('/');
			expr();
			expr();
		} else if( look.tag == Tag.ID){
			match(Tag.ID);
		} else if( look.tag == Tag.NUM){
			match(Tag.NUM);
		} else error("Errore sintattico.");	
    } //bexpr

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
        Lexer lex = new Lexer(); /* creazione nuovo lexer */
        String path = "C:/Users/lucar/OneDrive/Desktop/UNI/LFT/lft lab 21_22/3.2/prova3.2.txt"; // il percorso del file da leggere
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Parser parser = new Parser(lex, br); 
            parser.prog(); // inizio della parsificazione
            br.close();
        } catch (IOException e) {e.printStackTrace();}    
    } //main
}
