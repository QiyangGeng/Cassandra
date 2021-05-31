package com.efonian.cassandra.discord.commands;

import com.efonian.cassandra.discord.commands.annotation.DeclareCommandAccessLevel;
import com.efonian.cassandra.discord.commands.annotation.Disabled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Disabled
@ConditionalOnProperty(value = "cassandra.discord.init", havingValue = "true")
@DeclareCommandAccessLevel(accessLevel = CommandAccessLevel.FULL)
public class cmd_Print extends Command {
    // https://gist.github.com/matthewzring/9f7bbfd102003963f9be7dbcf7d40e51#syntax-highlighting
    private static final Map<String, String> fileExtToSyntaxName = new ConcurrentHashMap<>() {{
        put("asciidoc", "asciidoc");
        put("adoc", "asciidoc");
        put("asc", "asciidoc");
        put("ahk", "autohotkey");
        put("sh", "bash");
        put("coffee", "coffee");
        put("cpp", "cpp");
        put("cs", "cs");
        put("css", "css");
        put("diff", "diff");
        put("fix", "fix");
        put("glsl", "glsl");
        put("ini", "ini");
        put("json", "json");
        put("md", "md");
    }};
    
    private static final List<String> lang = List.of("1c","abnf","accesslog","actionscript","ada","angelscript",
            "apache","applescript","arcade","arduino","armasm","asciidoc","aspectj","autohotkey","autoit","avrasm",
            "awk","axapta","bash","basic","bnf","brainfuck","cal","capnproto","ceylon","clean","clojure","clojure-repl",
            "cmake","coffeescript","coq","cos","cpp","crmsh","crystal","cs","csp","css","d","dart","delphi","diff",
            "django","dns","dockerfile","dos","dsconfig","dts","dust","ebnf","elixir","elm","erb","erlang",
            "erlang-repl","excel","fix","flix","fortran","fsharp","gams","gauss","gcode","gherkin","glsl","gml","go",
            "golo","gradle","groovy","haml","handlebars","haskell","haxe","hsp","htmlbars","http","hy","inform7","ini",
            "irpf90","isbl","java","javascript","jboss-cli","json","julia","julia-repl","kotlin","lasso","ldif","leaf",
            "less","lisp","livecodeserver","livescript","llvm","lsl","lua","makefile","markdown","mathematica","matlab",
            "maxima","mel","mercury","mipsasm","mizar","mojolicious","monkey","moonscript","n1ql","nginx","nimrod",
            "nix","nsis","objectivec","ocaml","openscad","oxygene","parser3","perl","pf","pgsql","php","plaintext",
            "pony","powershell","processing","profile","prolog","properties","protobuf","puppet","purebasic","python",
            "q","qml","r","reasonml","rib","roboconf","routeros","rsl","ruby","ruleslanguage","rust","sas","scala",
            "scheme","scilab","scss","shell","smali","smalltalk","sml","sqf","sql","stan","stata","step21","stylus",
            "subunit","swift","taggerscript","tap","tcl","tex","thrift","tp","twig","typescript","vala","vbnet",
            "vbscript","vbscript-html","verilog","vhdl","vim","x86asm","xl","xml","xquery","yaml","zephir");
    
    @Override
    boolean execute(CommandContainer cc) {
        return false;
    }
    
    @Override
    protected void setup() {
        // read the strings from file here
    }
    
    @Override
    List<String> invokes() {
        return List.of("print");
    }
    
    @Override
    public String description() {
        return "wip";
    }
}
