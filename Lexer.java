import java.io.*; 
import java.util.*;

public class Lexer {

    public int line = 1;
    private char peek = ' ';
    public static String nmb;

    
    private void readch(BufferedReader br) {
        try {
            peek = (char) br.read();
        } catch (IOException exc) {
            peek = (char) -1; // ERROR
        }
    }

    public Token lexical_scan(BufferedReader br) {
        while (peek == ' ' || peek == '\t' || peek == '\n'  || peek == '\r') {
            if (peek == '\n') line++;
            readch(br);
        }
        
        switch (peek) {
            case '!':
                peek = ' ';
                return Token.not;

            case '(':
                peek = ' ';
                return Token.lpt;
            
            case ')':
                peek = ' ';
                return Token.rpt;
            
            case '{':
                peek = ' ';
                return Token.lpg;

            case '}':
                peek = ' ';
                return Token.rpg;
            
            case '+':
                peek = ' ';
                return Token.plus;

            case '-':
                peek = ' ';
                return Token.minus;

            case '*':
                peek = ' ';
                return Token.mult;
            case ',':
                peek = ' ';
                return Token.comma;
            /*case '/':
                peek = ' ';
                return Token.div;*/
            case '/':
                readch(br);
                if ((peek == '/')) {
                   return lexical_scan(br);
                } else if(peek=='*'){
                    readch(br);
                    boolean flag = false;
                    while ((peek=='\n') || (!flag)) {
                        if(peek == (char) -1) break;
						readch(br);
						if(peek=='*'){
							readch(br);
							if(peek=='/')
								flag = true;	
						} 
                    }	
					if(!flag){
						System.out.println("Errore causato dalla sezione commenti.");
						return new Token(Tag.EOF);	 
					} else {
						readch(br);
						return lexical_scan(br);
					}
                } else if(Character.isLetter(peek)) {
					return Token.div;
				} else if(Character.isDigit(peek)) {
					return Token.div;
				}else if ((peek != '/')) {
                    readch(br);
                    while ((peek!='\n')) {
                        readch(br);
                    }
                    readch(br);
                    return lexical_scan(br);
				}



            case ';':
                peek = ' ';
                return Token.semicolon;

	// ... gestire i casi di ( ) { } + - * / ; , ... //
	
            case '&':
                readch(br);
                if (peek == '&') {
                    peek = ' ';
                    return Word.and;
                } else {
                    System.err.println("Erroneous character"
                            + " after & : "  + peek );
                    return null;
                }

            

            case '|':
                readch(br);
                if (peek == '|') {
                    peek = ' ';
                    return Word.or;
                } else {
                    System.err.println("Erroneous character"
                            + " after | : "  + peek );
                    return null;
                }

            case ':':
                readch(br);
                if (peek == '=') {
                    peek = ' ';
                    return Word.assign;
                } else {
                    System.err.println("Erroneous character"
                            + " after : : "  + peek );
                    return null;
                }

            case '<':
                readch(br);
				switch(peek){
					case '=':
						peek = ' ';
						return Word.le;
					case '>':
						peek = ' ';
						return Word.ne;
					default:
                        return Word.lt;	
                }
            case '>':
                readch(br);
                switch(peek){
                    case '=':
                        peek = ' ';
                            return Word.ge;
                        default:
                            return Word.gt;	
                        }

            case '=':
                readch(br);
                switch(peek){
                    case '=':
                        peek = ' ';
                                return Word.eq;
                            default:
                                return Word.assign;	
                            }

	// ... gestire i casi di || < > <= >= == <> ... //
          
            case (char)-1:
                return new Token(Tag.EOF);

            default:
                if (Character.isLetter(peek)) {
                    String s = new String();
					boolean number = false;
					boolean undscore = false;
					while(Character.isLetter(peek) || (Character.isDigit(peek) || (peek == '_'))){
						s = s + peek;
						readch(br);
						if (Character.isDigit(peek))
							number = true;
						if ((peek == '_'))
							undscore = true;
					}	
					if(s.compareTo("print") == 0){
						return Word.print;
					} else if(s.compareTo("read") == 0){
						return Word.read;
					} else if(s.compareTo("assign") == 0){
						return Word.assign;
					} else if(s.compareTo("to") == 0){
						return Word.to;
					} else if(s.compareTo("if") == 0){
						return Word.iftok;
					} else if(s.compareTo("else")==0){
                        return Word.elsetok;
                    } else if(s.compareTo("while")==0){
                        return Word.whiletok;
                    } else if(s.compareTo("begin")==0){
                        return Word.begin;
                    }else if(s.compareTo("end")==0){
                        return Word.end;
                    }else{
						return new Word(Tag.ID, s);}

	// ... gestire il caso degli identificatori e delle parole chiave //

                } else if (Character.isDigit(peek)) {
                    int n = Character.getNumericValue(peek);
                    int r;
                    readch(br);
                    while (Character.isDigit(peek)) {
                        r = Character.getNumericValue(peek);
                        n = n*10+r;
                        readch(br);
                    }
					nmb = String.valueOf(n);
                    return new NumberTok(Tag.NUM, String.valueOf(n));
	// ... gestire il caso dei numeri ... //

                } else {
                        System.err.println("Erroneous character: " 
                                + peek );
                        return null;
                }
         }
        
        }
        
         public String getnum(){ // metodo per restituire il numero NUM.value per l'analizzatore sintattico
         return nmb;
     } //getnum
    
		
    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "C:/Users/lucar/OneDrive/Desktop/UNI/LFT/lft lab 21_22/3.2/prova3.2.txt"; // il percorso del file da leggere
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Token tok;
            do {
                tok = lex.lexical_scan(br);
                System.out.println("Scan: " + tok);
            } while (tok.tag != Tag.EOF);
            br.close();
        } catch (IOException e) {e.printStackTrace();}    
    }

}