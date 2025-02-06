package cn.leaf;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Main extends Application {

    public static final String TCP="TCP", UDP="UDP", ICMP="ICMP";

    @Override
    public void start(Stage primaryStage) {
        // 创建左边列表
        ListView<String> listView = new ListView<>();
        VBox.setVgrow(listView, Priority.ALWAYS);

        ChoiceBox<String> protocals=new ChoiceBox<>();
        protocals.getItems().addAll(TCP, UDP, ICMP);
        protocals.setValue(TCP);

        TextField source_ip = new TextField();
        TextField des_ip = new TextField();
        TextField port = new TextField();
        source_ip.setPromptText("源IP");
        des_ip.setPromptText("目的IP");
        port.setPromptText("端口");
        port.setPrefWidth(60);

        HBox empty_space = new HBox(10);
        empty_space.setPrefHeight(30);

//        HBox topTextBoxes = new HBox(50, protocals, source_ip, des_ip, port);
        HBox topTextBoxes = new HBox(30);
        topTextBoxes.getChildren().addAll(protocals, source_ip, des_ip, port);
        topTextBoxes.setPrefHeight(50); // 设置小一点的高度
        topTextBoxes.setPadding(new Insets(20));

        // 创建右边中间两个按钮
        Button check_btn = new Button("查询");
        Button clear_btn = new Button("清空");
        check_btn.setOnAction(e->{
            check_btn.setDisable(true); // 禁用按钮防止重复点击

            Stage loadingStage = new Stage();
            loadingStage.initModality(Modality.APPLICATION_MODAL);
            loadingStage.setTitle("操作中");

            Label loadingLabel = new Label("操作中...");
            Scene loadingScene = new Scene(new StackPane(loadingLabel), 200, 100);
            loadingStage.setScene(loadingScene);

            loadingStage.show();

            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        Thread.sleep(5000); // 模拟长时间运行的操作
                    } catch (InterruptedException ignored) {}
                    return null;
                }
            };

            task.setOnSucceeded(event -> {
                loadingStage.close();
                check_btn.setDisable(false); // 启用按钮
                
            });

            task.setOnFailed(event -> {
                loadingStage.close();
                check_btn.setDisable(false); // 启用按钮
            });

            new Thread(task).start();
        });

        HBox middleButtons = new HBox(10, check_btn, clear_btn);
        middleButtons.setAlignment(Pos.CENTER);
        middleButtons.setPrefHeight(50); // 设置小一点的高度

        // 创建右边底部只读文本框（用于输出执行日志）
        TextArea logTextArea = new TextArea();
        logTextArea.setEditable(false); // 只读
        VBox.setVgrow(logTextArea, Priority.ALWAYS);

        // 将上面的组件放入一个VBox中
        VBox rightPanel = new VBox(10,empty_space, topTextBoxes, middleButtons, logTextArea);
        VBox.setVgrow(rightPanel, Priority.ALWAYS);

        // 使用SplitPane分割左右两部分
        SplitPane splitPane = new SplitPane(listView, rightPanel);
        splitPane.setDividerPositions(0.3); // 左边占30%宽度


        // 设置场景
        BorderPane root = new BorderPane(splitPane);
        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("Security Group Detector");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}