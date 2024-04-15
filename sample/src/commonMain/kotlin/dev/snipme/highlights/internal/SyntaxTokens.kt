package dev.snipme.highlights.internal

internal object SyntaxTokens {
    val FLOW_CONTROL_KEYWORDS = "break,continue,do,else,for,if,return,while".split(",")

    val C_KEYWORDS = FLOW_CONTROL_KEYWORDS + ("auto,case,char,const,default,"
            + "double,enum,extern,float,goto,inline,int,long,register,short,signed,"
            + "sizeof,static,struct,switch,typedef,union,unsigned,void,volatile").split(",")

    val COMMON_KEYWORDS = C_KEYWORDS + ("catch,class,delete,false,import,"
            + "new,operator,private,protected,public,this,throw,true,try,typeof").split(",")

    val CPP_KEYWORDS = COMMON_KEYWORDS + ("alignof,align_union,asm,axiom,bool,"
            + "concept,concept_map,const_cast,constexpr,decltype,delegate,"
            + "dynamic_cast,explicit,export,friend,generic,late_check,"
            + "mutable,namespace,nullptr,property,reinterpret_cast,static_assert,"
            + "static_cast,template,typeid,typename,using,virtual,where").split(",")

    val JAVA_KEYWORDS = COMMON_KEYWORDS +
            ("abstract,assert,boolean,byte,extends,final,finally,implements,import,"
            + "instanceof,interface,null,native,package,strictfp,super,synchronized,"
            + "throws,transient").split(",")

    val KOTLIN_KEYWORDS = JAVA_KEYWORDS +
            ("as,as?,fun,in,!in,is,!is,object,typealias,val,var,when,by,constructor,delegate,dynamic,"
            + "file,get,init,set,value,where,actual,annotation,companion,crossinline,data,enum,expect,"
            + "external,field,infix,inline,inner,internal,lateinit,noinline,open,operator,out,override,"
            + "reified,sealed,suspend,tailrec,vararg").split(",")

    val RUST_KEYWORDS = FLOW_CONTROL_KEYWORDS + ( "as,assert,const,copy,drop,"
            + "enum,extern,fail,false,fn,impl,let,log,loop,match,mod,move,mut,priv,"
            + "pub,pure,ref,self,static,struct,true,trait,type,unsafe,use").split(",")

    val CSHARP_KEYWORDS = JAVA_KEYWORDS +
        ("as,base,by,checked,decimal,delegate,descending,dynamic,event,"
            + "fixed,foreach,from,group,implicit,in,internal,into,is,let,"
            + "lock,object,out,override,orderby,params,partial,readonly,ref,sbyte,"
            + "sealed,stackalloc,string,select,uint,ulong,unchecked,unsafe,ushort,"
            + "var,virtual,where").split(",")

    val COFFEE_KEYWORDS = ("all,and,by,catch,class,else,extends,false,finally,"
            + "for,if,in,is,isnt,loop,new,no,not,null,of,off,on,or,return,super,then,"
            + "throw,true,try,unless,until,when,while,yes").split(",")

    val JSCRIPT_KEYWORDS = COMMON_KEYWORDS +
        ("debugger,eval,export,function,get,null,set,undefined,var,with,"
            + "Infinity,NaN").split(",")

    val PERL_KEYWORDS = ("caller,delete,die,do,dump,elsif,eval,exit,foreach,for,"
            + "goto,if,import,last,local,my,next,no,our,print,package,redo,require,"
            + "sub,undef,unless,until,use,wantarray,while,BEGIN,END").split(",")

    val PYTHON_KEYWORDS = FLOW_CONTROL_KEYWORDS +
            ("and,as,assert,class,def,del,"
            + "elif,except,exec,finally,from,global,import,in,is,lambda,"
            + "nonlocal,not,or,pass,print,raise,try,with,yield,"
            + "False,True,None").split(",")

    val RUBY_KEYWORDS = FLOW_CONTROL_KEYWORDS +
            ("alias,and,begin,case,class,"
            + "def,defined,elsif,end,ensure,false,in,module,next,nil,not,or,redo,"
            + "rescue,retry,self,super,then,true,undef,unless,until,when,yield,"
            + "BEGIN,END").split(",")

    val SH_KEYWORDS = FLOW_CONTROL_KEYWORDS +
            ("case,done,elif,esac,eval,fi,"
            + "function,in,local,set,then,until").split(",")

    val SWIFT_KEYWORDS = FLOW_CONTROL_KEYWORDS + "," +
        "associatedtype,async,await,class,deinit,enum,extension,fileprivate," +
                "func,import,init,inout,internal,let,open,operator,private,protocol,public,rethrows,static," +
                "struct,subscript,typealias,andvar,case,default,defer,fallthrough," +
                "guard,in,repeat,switch,where,as,Any,catch,false,is,nil,super,self,Self," +
                "throw,throws,true,try,#available,#colorLiteral,#column,#else,#elseif,#endif,#error,#file," +
                "#fileID,#fileLiteral,#filePath,#function,#if,#imageLiteral,#line,#selector,#sourceLocation," +
                "#warning,associativity,convenience,dynamic,didSet,final,get,infix,indirect,lazy,left," +
                "mutating,none,nonmutating,optional,override,postfix,precedence,prefix,Protocol,required," +
                "right,set,Type,unowned,weak,willSet,var,_".split(",")

    val ALL_KEYWORDS = (CPP_KEYWORDS +  KOTLIN_KEYWORDS +  CSHARP_KEYWORDS
            + RUST_KEYWORDS +  COFFEE_KEYWORDS
            +  JSCRIPT_KEYWORDS +  PERL_KEYWORDS +  PYTHON_KEYWORDS +  RUBY_KEYWORDS
            +  SH_KEYWORDS + SWIFT_KEYWORDS)

    val ALL_MIXED_KEYWORDS: List<String> =
        """#available #column #define #defined #elif #else #else#elseif #endif #error #file #function 
                 #if #ifdef #ifndef #include #line #pragma #selector #undef abstract add after alias 
                 alignas alignof and and_eq andalso as ascending asm assert associatedtype associativity 
                 async atomic_cancel atomic_commit atomic_noexcept auto await base become begin bitand 
                 bitor bnot bor box break bsl bsr bxor case catch chan  
                 checked class compl concept cond const const_cast constexpr continue convenience 
                 covariant crate debugger decltype def default defer deferred defined? deinit 
                 del delegate delete descending didset div do dynamic dynamic_cast dynamictype 
                 elif else elseif elsif end ensure eval event except explicit export extends extension 
                 extern external factory fallthrough false final finally fixed fn for foreach friend 
                 from fun func function get global go goto group guard if impl implements implicit import 
                 in indirect infix init inline inout instanceof interface internal into is join lambda 
                 lazy left let library local lock long loop macro map match mod module move mut mutable 
                 mutating namespace native new next nil noexcept none nonlocal nonmutating not not_eq 
                 null nullptr object of offsetof operator optional or or_eq orderby orelse out override 
                 package params part partial pass postfix precedence prefix priv private proc protected 
                 protocol pub public pure raise range readonly receive redo ref register reinterpret_cast 
                 rem remove repeat required requires rescue rethrow rethrows retry return right sbyte 
                 sealed select self set short signed sizeof stackalloc static static_assert static_cast 
                 strictfp struct subscript super switch sync synchronized template then this 
                 thread_local throw throws trait transaction_safe transaction_safe_dynamic transient 
                 true try type typealias typedef typeid typename typeof uint ulong unchecked undef 
                 union unless unowned unsafe unsigned unsized until use ushort using value var virtual 
                 void volatile wchar_t weak when where while willset with xor xor_eq xorauto yield 
                 yieldabstract yieldarguments val list override get set as as? in !in !is is by 
                 constructor delegate dynamic field file init param property receiver setparam data 
                 data expect lateinit crossinline companion annotation actual noinline open reified 
                 suspend tailrec vararg it constraint alter column table all any asc backup database 
                 between check create index replace view procedure unique desc distinct drop exec 
                 exists foreign key full outer having inner insert like limit order primary rownum 
                 top truncate update values"""
            .split(" ")

    // TODO Migrate to list of chars
    val TOKEN_DELIMITERS = listOf(" ", ",", ".", ":", ";", "(", ")", "=", "{", "}", "<", ">", "\r", "\n")
    val STRING_DELIMITERS = listOf("\'", "\"", "\"\"\"")
    val COMMENT_DELIMITERS = listOf("//", "#")
    // TODO Add support for other other languages like Dart or Python
    val MULTILINE_COMMENT_DELIMITERS = listOf(Pair("/*", "*/"))
    val PUNCTUATION_CHARACTERS = listOf(",", ".", ":", ";")
    val MARK_CHARACTERS = listOf("(", ")", "=", "{", "}", "<", ">", "-", "+", "[", "]", "|", "&")
}