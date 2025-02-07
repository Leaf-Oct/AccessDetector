package cn.leaf;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainWindow extends Application {

    public static final String TCP="TCP", UDP="UDP", ICMP="ICMP";

//    左边列表
    public ListView<String> listView= new ListView<>();
//    四元组
//    下拉列表框
    public ChoiceBox<String> protocals=new ChoiceBox<>();
//    源，目的IP和端口
    public TextField source_ip = new TextField();
    public TextField des_ip = new TextField();
    public TextField port = new TextField();
//    三个按钮
    public Button check_btn = new Button("查询");
    public Button clear_btn = new Button("清空");
    public Button clear_history_btn=new Button("清除历史");
    public TextArea log_text_area = new TextArea();

    public Stage loading_stage = new Stage();
    public Scene loading_scene, main_scene;
    @Override
    public void init() throws Exception {
        super.init();
//        AppCore.init();
        loadingSceneInit();
        mainSceneInit();
        AppCore.log_area=log_text_area;
    }

    @Override
    public void start(Stage primaryStage) {

//        点击后的查询操作
        check_btn.setOnAction(e->{
            check_btn.setDisable(true); // 禁用按钮防止重复点击
            loading_stage.show();

            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    var protocal=protocals.getValue();
                    var s_ip=source_ip.getText();
                    var d_ip=des_ip.getText();
                    var p=port.getText();
                    var check_item=new String[4];
                    check_item[0]=protocal;
                    check_item[1]=s_ip;
                    check_item[2]=d_ip;
                    check_item[3]=p;
                    if (!AppCore.checkRule(check_item)){
                        throw new Exception("输入不合法");
                    }
                    return null;
                }
            };

            task.setOnSucceeded(event -> {
                loading_stage.close();
                check_btn.setDisable(false); // 启用按钮
                
            });
            task.setOnFailed(event -> {
                loading_stage.close();
                check_btn.setDisable(false); // 启用按钮
            });
            new Thread(task).start();
        });
        clear_btn.setOnAction(e->{
            source_ip.clear();
            des_ip.clear();
            port.clear();
        });

        loading_stage.setScene(loading_scene);
        loading_stage.initStyle(StageStyle.UNDECORATED);

        primaryStage.setTitle("Access Detector");
        primaryStage.setScene(main_scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void mainSceneInit(){
        //      设置左边
        VBox.setVgrow(listView, Priority.ALWAYS);
        HBox.setHgrow(clear_history_btn, Priority.ALWAYS);
        clear_history_btn.setMinWidth(Region.USE_PREF_SIZE);
        clear_history_btn.setPrefWidth(Region.USE_COMPUTED_SIZE);
        clear_history_btn.setMaxWidth(Double.MAX_VALUE);

//        设置右边
        protocals.getItems().addAll(TCP, UDP, ICMP);
        protocals.setValue(TCP);

//        三元组文本框
        source_ip.setPromptText("源IP");
        des_ip.setPromptText("目的IP");
        port.setPromptText("端口");
        port.setPrefWidth(60);
//        一点空间
        HBox empty_space = new HBox(10);
        empty_space.setPrefHeight(30);
//        四元组放到一个HBOX里
        HBox topTextBoxes = new HBox(30);
        topTextBoxes.getChildren().addAll(protocals, source_ip, des_ip, port);
        topTextBoxes.setPrefHeight(50); // 设置小一点的高度

        HBox middleButtons = new HBox(10, check_btn, clear_btn);
        middleButtons.setAlignment(Pos.CENTER);
        middleButtons.setPrefHeight(50); // 设置小一点的高度

        // 创建右边底部只读文本框（用于输出执行日志）
        log_text_area.setEditable(false); // 只读
        VBox.setVgrow(log_text_area, Priority.ALWAYS);

        // 将上面的组件放入一个VBox中
        VBox rightPanel = new VBox(10,empty_space, topTextBoxes, middleButtons, log_text_area);
        VBox.setVgrow(rightPanel, Priority.ALWAYS);
        VBox leftPanel=new VBox(10, listView, clear_history_btn);
        rightPanel.setPadding(new Insets(10));
        leftPanel.setPadding(new Insets(10));

        // 使用SplitPane分割左右两部分
        SplitPane splitPane = new SplitPane(leftPanel, rightPanel);
        splitPane.setDividerPositions(0.3); // 左边占30%宽度
        // 设置场景
        BorderPane root = new BorderPane(splitPane);
        main_scene= new Scene(root, 800, 600);
    }

    private void loadingSceneInit(){
        Image gifImage = new Image(getClass().getResource("/初始化.gif").toString());
        ImageView imageView = new ImageView(gifImage);
        Label tip = new Label("查询中");
        tip.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-background-color: rgba(0, 0, 0, 0.5);"); // 设置样式使其更易读
        StackPane root = new StackPane();
        root.getChildren().addAll(imageView, tip);
        StackPane.setAlignment(tip, javafx.geometry.Pos.BOTTOM_CENTER);
        StackPane.setMargin(tip, new javafx.geometry.Insets(0, 0, 20, 0)); // 下边距
        loading_scene = new Scene(root, 480, 360);
    }
}