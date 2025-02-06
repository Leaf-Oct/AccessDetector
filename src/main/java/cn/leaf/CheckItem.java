package cn.leaf;

public class CheckItem {
    public String from, to, port, result, protocal;

    public CheckItem(String port, String to, String from, String protocal) {
        this.port = port;
        this.to = to;
        this.from = from;
        this.protocal = protocal;
    }
}

