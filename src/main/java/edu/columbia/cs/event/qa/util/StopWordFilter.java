package edu.columbia.cs.event.qa.util;

public class StopWordFilter {

	public static String filter( String word ){        //Stop word removal

		if (word.equals("a") || word.equals("about") || word.equals("above") || word.equals("above") || word.equals("across") || word.equals("after")
				|| word.equals("afterwards") || word.equals("again") || word.equals("against") || word.equals("all") || word.equals("almost")
				|| word.equals("alone") || word.equals("along") || word.equals("already")
				|| word.equals("also") || word.equals("although") || word.equals("always") || word.equals("am") || word.equals("among") || word.equals("amongst")
				|| word.equals("amoungst") || word.equals("an") || word.equals("and") || word.equals("another")
				|| word.equals("any") || word.equals("anyhow") || word.equals("anyone") || word.equals("anything") || word.equals("anyway") || word.equals("anywhere")
				|| word.equals("are") || word.equals("around") || word.equals("as") || word.equals("at") || word.equals("back") || word.equals("be") || word.equals("became")
				|| word.equals("because") || word.equals("become") || word.equals("becomes") || word.equals("becoming") || word.equals("been")
				|| word.equals("before") || word.equals("beforehand") || word.equals("behind") || word.equals("being") || word.equals("below")
				|| word.equals("beside") || word.equals("besides") || word.equals("between") || word.equals("beyond") || word.equals("bill")
				|| word.equals("both") || word.equals("bottom") || word.equals("but") || word.equals("by") || word.equals("call") || word.equals("can") 
				|| word.equals("cannot") || word.equals("cant") || word.equals("co") || word.equals("con") || word.equals("could") || word.equals("couldnt") || word.equals("cry") || word.equals("de")
				|| word.equals("detail") || word.equals("do") || word.equals("done") || word.equals("down") || word.equals("due")
				|| word.equals("during") || word.equals("each") || word.equals("eg") || word.equals("eight") || word.equals("either")
				|| word.equals("eleven") || word.equals("else") || word.equals("elsewhere") || word.equals("empty") || word.equals("enough") || word.equals("etc")
				|| word.equals("even") || word.equals("ever") || word.equals("every") || word.equals("everyone") || word.equals("everything")
				|| word.equals("everywhere") || word.equals("except") || word.equals("few") || word.equals("fill") || word.equals("find") || word.equals("fire") || word.equals("first") || word.equals("five") || word.equals("for")
				|| word.equals("former") || word.equals("formerly") || word.equals("forty") || word.equals("found") || word.equals("four") || word.equals("from")
				|| word.equals("front") || word.equals("full") || word.equals("further") || word.equals("get") || word.equals("give") || word.equals("go") || word.equals("had")
				|| word.equals("has") || word.equals("hasnt") || word.equals("have") || word.equals("he") || word.equals("hence") || word.equals("her") || word.equals("here")
				|| word.equals("hereafter") || word.equals("hereby") || word.equals("herein") || word.equals("hereupon") || word.equals("hers")
				|| word.equals("herself") || word.equals("him") || word.equals("himself") || word.equals("his") || word.equals("how") || word.equals("however")
				|| word.equals("hundred") || word.equals("ie") || word.equals("if") || word.equals("in") || word.equals("inc") || word.equals("indeed")
				|| word.equals("interest") || word.equals("into") || word.equals("is") || word.equals("it") || word.equals("its") || word.equals("itself") || word.equals("keep")
				|| word.equals("last") || word.equals("latter") || word.equals("latterly") || word.equals("least") || word.equals("less") || word.equals("ltd")
				|| word.equals("made") || word.equals("many") || word.equals("may") || word.equals("me") || word.equals("meanwhile") || word.equals("might")
				|| word.equals("mill") || word.equals("mine") || word.equals("more") || word.equals("moreover") || word.equals("most") || word.equals("mostly")
				|| word.equals("move") || word.equals("much") || word.equals("must") || word.equals("my") || word.equals("myself") || word.equals("name")
				|| word.equals("namely") || word.equals("neither") || word.equals("never") || word.equals("nevertheless") || word.equals("new") || word.equals("news") || word.equals("next")
				|| word.equals("nine") || word.equals("no") || word.equals("nobody") || word.equals("none") || word.equals("noone") || word.equals("nor") || word.equals("not")
				|| word.equals("nothing") || word.equals("now") || word.equals("nowhere") || word.equals("of") || word.equals("off") || word.equals("often") || word.equals("on")
				|| word.equals("once") || word.equals("one") || word.equals("only") || word.equals("onto") || word.equals("or") || word.equals("other") || word.equals("others")
				|| word.equals("otherwise") || word.equals("our") || word.equals("ours") || word.equals("ourselves") || word.equals("out") || word.equals("over")
				|| word.equals("own") || word.equals("part") || word.equals("per") || word.equals("perhaps") || word.equals("please") || word.equals("put")
				|| word.equals("rather") || word.equals("re") || word.equals("said") || word.equals("same") || word.equals("say") || word.equals("see") || word.equals("seem") || word.equals("seemed")
				|| word.equals("seeming") || word.equals("seems") || word.equals("serious") || word.equals("several") || word.equals("she")
				|| word.equals("should") || word.equals("show") || word.equals("side") || word.equals("since") || word.equals("sincere") || word.equals("six")
				|| word.equals("sixty") || word.equals("so") || word.equals("some") || word.equals("somehow") || word.equals("someone")
				|| word.equals("something") || word.equals("sometime") || word.equals("sometimes") || word.equals("somewhere")
				|| word.equals("still") || word.equals("such") || word.equals("system") || word.equals("take") || word.equals("ten") || word.equals("than")
				|| word.equals("that") || word.equals("the") || word.equals("their") || word.equals("them") || word.equals("themselves") || word.equals("then")
				|| word.equals("thence") || word.equals("there") || word.equals("thereafter") || word.equals("thereby") || word.equals("therefore")
				|| word.equals("therein") || word.equals("thereupon") || word.equals("these") || word.equals("they") || word.equals("thickv")
				|| word.equals("thin") || word.equals("third") || word.equals("this") || word.equals("those") || word.equals("though") || word.equals("three")
				|| word.equals("through") || word.equals("throughout") || word.equals("thru") || word.equals("thus") || word.equals("to")
				|| word.equals("together") || word.equals("too") || word.equals("top") || word.equals("toward") || word.equals("towards") || word.equals("twelve")
				|| word.equals("twenty") || word.equals("two") || word.equals("un") || word.equals("under") || word.equals("until") || word.equals("up") || word.equals("upon")
				|| word.equals("us") || word.equals("very") || word.equals("via") || word.equals("was") || word.equals("we") || word.equals("well") || word.equals("were")
				|| word.equals("what") || word.equals("whatever") || word.equals("when") || word.equals("whence") || word.equals("whenever")
				|| word.equals("where") || word.equals("whereafter") || word.equals("whereas") || word.equals("whereby") || word.equals("wherein")
				|| word.equals("whereupon") || word.equals("wherever") || word.equals("whether") || word.equals("which") || word.equals("while")
				|| word.equals("whither") || word.equals("who") || word.equals("whoever") || word.equals("whole") || word.equals("whom") || word.equals("whose") 
				|| word.equals("why") || word.equals("will") || word.equals("with") || word.equals("within") || word.equals("without") || word.equals("would")
				|| word.equals("yet") || word.equals("you") || word.equals("your") || word.equals("yours") || word.equals("yourself") || word.equals("yourselves")
				|| word.equals("the"))
			return "";
		else
			return word;



		/*if( word.equals("the")  || word.equals("i")    || word.equals("you")   ||
          word.equals("he")   || word.equals("she")  || word.equals("are")   ||   
          word.equals("it")   || word.equals("we")   || word.equals("am")    || 
          word.equals("they") || word.equals("is")   || word.equals("was")   ||
          word.equals("has")  || word.equals("had")  || word.equals("were")  ||
          word.equals("be")   || word.equals("been") || word.equals("on")    ||   
          word.equals("by")   || word.equals("to")   || 
          //word.equals("up")    || 
          word.equals("my")   || word.equals("your") || word.equals("its")   //||
          //word.equals("but")  
          || word.equals("our")  || word.equals("their") ||
          word.equals("a")    || word.equals("an")   || word.equals("at")    ||   
          word.equals("of")   || word.equals("and")  || word.equals("in")    ||
          word.equals("per")  
          || word.equals("or")   || word.equals("as")    ||
          word.equals("there")|| word.equals("this") || word.equals("these") ||
          word.equals("for")  || 
          word.equals("any")  || 
          word.equals("if")    ||
          word.equals("")     || word.length()<=2    || word.equals("that")  ||
          word.equals("than") || word.equals("with") || word.equals("which")


      )return "";

      else 
       return word;   */

	}
}