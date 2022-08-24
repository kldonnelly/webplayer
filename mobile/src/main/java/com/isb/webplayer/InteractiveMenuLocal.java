package com.isb.webplayer;

public class InteractiveMenuLocal implements IInteractiveMenuInjection{


    private String menu="var loading=document.querySelector(\".loading\");\n" +
            "if(loading!=null)loading.remove();\n" +
            "\n" +
            "if (!document.querySelector(\".back-button\")) {\n" +
            "    var divbackbut = document.createElement('div');\n" +
            "    divbackbut.className = 'back-button';\n" +
            "    divbackbut.innerHTML = \"&#10096; Back\";\n" +
            "    divbackbut.style.cursor = \"pointer\";\n" +
            "    divbackbut.style.left = \"0\";\n" +
            "    divbackbut.style.bottom = \"0\";\n" +
            "    divbackbut.style.position = \"fixed\";\n" +
            "    divbackbut.style.padding = \"10px 20px\";\n" +
            "    divbackbut.style.color = \"#FFFFFF\";\n" +
            "    divbackbut.style.background = \"linear-gradient(90deg, black, transparent)\";\n" +
            "    divbackbut.style.zIndex = \"9999\";\n" +
            "    divbackbut.style.height = \"50px\";\n" +
            "    divbackbut.style.fontSize=\"32px\";\n" +
            "\n" +
            "  \n" +
            "    var body = document.getElementsByTagName('body')[0];\n" +
            "    if(body!=null)body.insertBefore(divbackbut, body.childNodes[0]);\n" +
            "  \n" +
            "    divbackbut.addEventListener('click', function () { window.history.back(); });\n" +
            "  }";

public String loading= "var divmessage = document.createElement('div');\n" +
        "  divmessage.className = 'loading';\n" +
        "  divmessage.innerHTML = \"%s\";\n" +
        "  divmessage.style.cursor = \"pointer\";\n" +
        "  divmessage.style.left = \"%dpx\";\n" +
        "  divmessage.style.top = \"%dpx\";\n" +
        "  divmessage.style.position = \"fixed\";\n" +
        "  divmessage.style.padding = \"0.5rem 1rem\";\n" +
        "  divmessage.style.fontSize = \"2rem\";\n" +
        "  divmessage.style.color = \"#FFFFFF\";\n" +
        "  divmessage.style.background = \"linear-gradient(90deg, black, transparent)\";\n" +
        "  divmessage.style.zIndex = \"9999\";\n" +
        "  var body = document.getElementsByTagName('body')[0];\n" +
        "  if(body!=null)body.insertBefore(divmessage, body.childNodes[0]);\n";

    @Override
    public String getjavascript() {
        return menu;
    }

    @Override
    public String getjavascript(boolean muted) {

        return menu + "document.getElementsByTagName('video')[0].muted=true;";
    }

    @Override
    public String getjavascript(int x, int y,String text) {
        return String.format(loading,text,x,y);
    }
}
