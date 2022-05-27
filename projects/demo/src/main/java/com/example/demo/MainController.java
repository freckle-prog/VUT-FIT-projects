package com.example.demo;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.ResourceBundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainController {
    public File selected_file;
    public String sequence_path;
    public final Background focusBackground = new Background( new BackgroundFill( Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY ) );
    public final Background unfocusBackground = new Background( new BackgroundFill( Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY ) );
    @FXML
    public ResourceBundle resources;

    @FXML
    public URL location;
    public MenuItem close_button;

    @FXML
    private AnchorPane main_view;

    @FXML
    public ListView list_view;

    @FXML
    private ContextMenu context_menu;

    @FXML
    public MenuItem draw_button;

    public double startDragX;
    public double startDragY;
    public boolean dragged = false;
    public double lastDragX;
    public double lastDragY;

    @FXML
    void remove_file_from_view (ActionEvent event) {
        close_button.setOnAction(e -> {
            final int selected_f_id = list_view.getSelectionModel().getSelectedIndex();
            if (selected_f_id != -1) {
                //String item_to_remove = list_view.getSelectionModel().getSelectedItem();
                final int new_selected_fid;
                if (selected_f_id == list_view.getItems().size() - 1) {
                    new_selected_fid = selected_f_id - 1;
                }
                else {
                    new_selected_fid = selected_f_id;
                }
                list_view.getItems().remove(selected_f_id);
                list_view.getSelectionModel().select(new_selected_fid);
            }
        });
    }

    @FXML
    int open_file(ActionEvent event) throws IOException {
        FileChooser fc = new FileChooser();
        selected_file = fc.showOpenDialog(null);

        if (selected_file == null) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Problem during opening file");
            errorAlert.showAndWait();
            return -1;
        }
        else {
            if (selected_file.getName().contains("json")) {
                list_view.getItems().add(selected_file.getName());
                String path = selected_file.getPath();

            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Bad file format");
                errorAlert.setHeaderText("Please, choose .json file format");
                errorAlert.showAndWait();
                return -1;
            }
        }
        return 0;
    }

    @FXML
    void initialize() {

    }

    @FXML
    void draw_new_scene(ActionEvent event) throws IOException {
        //*** JUST MENU STUFFS ****//
        Group root = new Group();
        Menu menu = new Menu("Edit");
        Menu menu1 = new Menu("Help");
        Menu menu2 = new Menu("File");
        Button back = new Button("Back to main menu");
        back.setLayoutX(630);
        back.setLayoutY(574);
        //JUST EDIT STUFFS
        MenuItem item1 = new MenuItem("Un-do");
        MenuItem item2 = new MenuItem("Copy");
        MenuItem item3 = new MenuItem("Cut");
        MenuItem item4 = new MenuItem("Delete");
        //JUST FILE STUFF
        MenuItem item5 = new MenuItem("Save");
        MenuItem item6 = new MenuItem("New sequence diagram");
        //INTEGRATION
        menu2.getItems().addAll(item6, item5);
        menu.getItems().addAll(item1, item2, item3, item4);
        Menu menu3 = new Menu("Actions");
        MenuItem m_item1 = new MenuItem("Draw new box");
        MenuItem m_item2 = new MenuItem("Create new line");
        menu3.getItems().addAll(m_item1, m_item2);
        MenuBar menu_bar = new MenuBar(menu2, menu, menu3, menu1);
        Scene scene = new Scene(root, 750, 600);
        root.getChildren().addAll(menu_bar, back);
        String path = selected_file.getPath();
        Gson json = new Gson();
        int delay = 0;
        int delay_sec = 0;
        JsonDiagram diagram = json.fromJson(new FileReader(path), JsonDiagram.class);

        //***************** JUST DIAGRAM ******************//
        Pane pane = new Pane();
        pane.setLayoutY(25);
        pane.setPrefHeight(Region.USE_COMPUTED_SIZE);
        pane.setPrefWidth(Region.USE_COMPUTED_SIZE);
        create_classes(diagram, pane, delay, delay_sec);

        root.getChildren().add(pane);
        // Communication between boxes //

        Stage stage = new Stage();
        stage.setTitle("UML Editor");
        stage.getIcons().add(new Image("C:\\Users\\lebed\\projects\\demo\\lib\\IMG_20200306_172134.jpg"));
        stage.setScene(scene);
        stage.show();

        //******** File -> Save file ***************//
        item5.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save file");
                File file = fileChooser.showSaveDialog(stage);
                if (file != null) {
                    String path = file.getPath();
                    try {
                        Gson json = new GsonBuilder().setPrettyPrinting().create();
                        Writer writer = new FileWriter(path);
                        writer.write(json.toJson(diagram));
                        writer.close();
                    } catch (Exception e) {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setHeaderText("Error during saving");
                        errorAlert.showAndWait();
                    }
                }
                else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setHeaderText("Error during saving");
                    errorAlert.showAndWait();
                }
            }
        });

        //******* BUTTON -> Back to main menu *********//
        back.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Stage stage = (Stage) back.getScene().getWindow();
                stage.close();
            }
        });

        //*** Create sequence diagram ***//
        item6.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("Choose next step");
                alert.setTitle("Sequence diagram");
                ButtonType open = new ButtonType("Open file");
                ButtonType create = new ButtonType("Create empty");
                alert.getButtonTypes().addAll(open, create);
                Optional <ButtonType> res = alert.showAndWait();
                if (res.get().equals(open)) {
                    FileChooser file_chooser = new FileChooser();
                    file_chooser.setTitle("Open new file");
                    File seq_file = file_chooser.showOpenDialog(null);
                    if (seq_file == null) {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setHeaderText("Problem during opening file");
                        errorAlert.showAndWait();
                    }
                    else {
                        if (seq_file.getName().contains("json")) {
                            sequence_path = seq_file.getPath();
                            Gson gson = new Gson();
                            try {
                                JsonDiagram seq_diagram = gson.fromJson(new FileReader(sequence_path), JsonDiagram.class);
                                draw_sequence_screen(seq_diagram, diagram);
                            } catch (FileNotFoundException ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("Bad file format");
                            errorAlert.setHeaderText("Please, choose .json file format");
                            errorAlert.showAndWait();
                        }
                    }
                }
                else if (res.get().equals(create)) {
                    ////JUST CREATE NEW :void:
                    draw_empty_sequence(diagram);
                }
            }
        });

        //**************** Actions -> Draw new box *****************//
        m_item1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TextInputDialog new_class_name = new TextInputDialog();
                new_class_name.setTitle("New class");
                new_class_name.setHeaderText("Enter new class name");
                new_class_name.showAndWait();
                String name = new_class_name.getEditor().getText();
                diagram.createClass(name);
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("New class");
                confirm.setHeaderText("Do you want to add some new attributes or operations?");
                ButtonType attrib_bttn = new ButtonType("Attributes");
                ButtonType oper_bttn = new ButtonType("Operations");
                ButtonType cancel_bttn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);//cancel button
                confirm.getButtonTypes().setAll(attrib_bttn, oper_bttn, cancel_bttn);
                Optional<ButtonType> result = confirm.showAndWait();
                if (result.get().equals(attrib_bttn)) {
                    TextInputDialog new_atrrib = new TextInputDialog();
                    new_atrrib.setTitle("New class");
                    new_atrrib.setHeaderText("Please, enter new attributes for class. Use format name:data_type");
                    new_atrrib.showAndWait();
                    String attribute = new_atrrib.getEditor().getText();
                    String[] attributes = attribute.split(":");
                    for (int i = 0; i < diagram.diagram_classes.size(); i++) {
                        if (diagram.diagram_classes.get(i).getClass_name().equals(name)) {
                            diagram.diagram_classes.get(i).addAttribute(new UMLAttribute(attributes[0], attributes[1]));
                        }
                    }
                }
                else if (result.get().equals(oper_bttn)) {
                    TextInputDialog new_atrrib = new TextInputDialog();
                    new_atrrib.setTitle("New class");
                    new_atrrib.setHeaderText("Please, enter new operations for class. Use format: name, input type, parameters");
                    new_atrrib.showAndWait();
                    String[] operation = new_atrrib.getEditor().getText().split(",");
                    for (int i = 0; i < diagram.diagram_classes.size(); i++) {
                        if (diagram.diagram_classes.get(i).getClass_name().equals(name)) {
                            diagram.diagram_classes.get(i).addArgument(new UMLOperation(operation[0], operation[1]));
                            for (int j = 0; j < diagram.diagram_classes.get(i).operations.size(); j++) {
                                if (diagram.diagram_classes.get(i).operations.get(j).getOp_name().equals(operation[0])) {
                                    for (int k = 2; k < operation.length; k++) {
                                        diagram.diagram_classes.get(i).operations.get(j).input_params.add(operation[k]);
                                    }
                                }
                            }
                        }
                    }
                }
                create_classes(diagram, pane, delay, delay_sec);
            }
        });
    }

    //TODO
    private void draw_empty_sequence(JsonDiagram diagram) {
        Group seq_root = new Group();
        Menu menu = new Menu("File");
        MenuItem item1 = new MenuItem("Save");

        Menu menu1 = new Menu("Edit");
        MenuItem item2 = new MenuItem("Add new object");
        MenuItem item3 = new MenuItem("Add new message");
        menu.getItems().addAll(item1);
        menu1.getItems().addAll(item2, item3);

        MenuBar menu_bar = new MenuBar(menu, menu1);
        seq_root.getChildren().add(menu_bar);
        Scene seq_scene = new Scene(seq_root, 750, 600);

        JsonDiagram empty_seq = new JsonDiagram("json_sequences");

        Pane pane = new Pane();
        pane.setLayoutY(25);
        pane.setPrefWidth(Region.USE_COMPUTED_SIZE);
        pane.setPrefHeight(Region.USE_COMPUTED_SIZE);
        create_sequence(empty_seq, pane, 0, diagram);

        seq_root.getChildren().add(pane);
        Stage seq_stage = new Stage();
        seq_stage.setTitle("UML Editor");
        seq_stage.getIcons().add(new Image("C:\\Users\\lebed\\projects\\demo\\lib\\IMG_20200306_172134.jpg"));
        seq_stage.setScene(seq_scene);
        seq_stage.show();

        item2.setOnAction(e -> {
            TextInputDialog new_line_name = new TextInputDialog();
            new_line_name.setTitle("New object name");
            new_line_name.setHeaderText("Enter name for new object");
            Optional<String> result = new_line_name.showAndWait();
            if (result.isEmpty()) {
                return;
            }
            String name = new_line_name.getEditor().getText();
            empty_seq.createClass(name);
            String name_sec = "";
            double new_posX = 50;
            double offset = 150;
            for (int i = 0; i < empty_seq.diagram_classes.size(); i++) {
                for (int j = 0; j < empty_seq.diagram_classes.get(i).json_sequences.size(); j++) {
                    if (empty_seq.diagram_classes.get(i).json_sequences.get(j).send_class.class_name.equals(name)) {
                        Alert exists = new Alert(Alert.AlertType.ERROR);
                        exists.setHeaderText("Object with this name already exists, do you want change name?");
                        ButtonType yes = new ButtonType("Yes");
                        ButtonType no = new ButtonType("No");
                        exists.getButtonTypes().addAll(yes, no);
                        Optional<ButtonType> res = exists.showAndWait();
                        if (res.get().equals(yes)) {
                            TextInputDialog new_name_sec = new TextInputDialog();
                            new_line_name.setTitle("New object name");
                            new_line_name.setHeaderText("Enter name for new object");
                            new_line_name.showAndWait();
                            name_sec = new_line_name.getEditor().getText();
                        }
                        else if (res.get().equals(no)) {
                            return;
                        }
                    }
                    else {
                        new_posX = 50 + offset;
                    }
                }
                if (!name_sec.equals("")) {
                    JsonSequence new_seq = new JsonSequence(name_sec, 300, new_posX);
                    empty_seq.diagram_classes.get(i).json_sequences.add(new_seq);
                }
                else {
                    JsonSequence new_seq = new JsonSequence(name, 300, new_posX);
                    empty_seq.diagram_classes.get(i).json_sequences.add(new_seq);
                }
            }
            offset += 150;
            create_sequence(empty_seq, pane, offset, diagram);
        });

        item3.setOnAction(e -> {
            Dialog<Object> dialog = new Dialog<>();
            dialog.setTitle("Message settings");
            dialog.setHeaderText("Choose the communicating classes");
            dialog.setResizable(true);
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            ComboBox from_class = new ComboBox<>();
            ComboBox to_class = new ComboBox<>();
            TextField message = new TextField();
            for (int i = 0; i < empty_seq.diagram_classes.size(); i++) {
                for (int j = 0; j < empty_seq.diagram_classes.get(i).json_sequences.size(); j++){
                    if (!from_class.getItems().contains(empty_seq.diagram_classes.get(i).json_sequences.get(j).send_class.class_name)) {
                        from_class.getItems().add(empty_seq.diagram_classes.get(i).json_sequences.get(j).send_class.class_name);
                    }
                    if (!to_class.getItems().contains(empty_seq.diagram_classes.get(i).json_sequences.get(j).send_class.class_name)) {
                        to_class.getItems().add(empty_seq.diagram_classes.get(i).json_sequences.get(j).send_class.class_name);
                    }
                }
            }
            dialogPane.setContent(new VBox(8, from_class, to_class, message));
            dialog.showAndWait();
            //TODO
            int id_seq = 1;
            for (int i = 0; i < empty_seq.diagram_classes.size(); i++) {
                for (int j = 0; j < empty_seq.diagram_classes.get(i).json_sequences.size(); j++){
                    if (empty_seq.diagram_classes.get(i).json_sequences.get(j).send_class.class_name.equals(from_class.getValue().toString())) {
                        for (int k = 0; k < empty_seq.diagram_classes.get(i).json_sequences.size(); k++) {
                            JsonSequence new_inv_seq = new JsonSequence(empty_seq.diagram_classes.get(i).json_sequences.get(k).send_class.class_name, 300,  empty_seq.diagram_classes.get(i).json_sequences.get(k).send_class.positionX);
                            empty_seq.diagram_classes.get(i).json_sequences.add(new_inv_seq);
                            if (empty_seq.diagram_classes.get(i).json_sequences.get(k).involved_class.class_name.equals(to_class.getValue().toString())){
                                double posX = empty_seq.diagram_classes.get(i).json_sequences.get(k).involved_class.positionX;
                                String seq_name = "New_user_seq" + id_seq;
                                JsonSequence new_seq_to_class = new JsonSequence(seq_name, empty_seq.diagram_classes.get(i).json_sequences.get(j).send_class.class_name, empty_seq.diagram_classes.get(i).json_sequences.get(k).involved_class.class_name, empty_seq.diagram_classes.get(i).json_sequences.get(j).send_class.positionX, posX, 300, message.getText());
                                empty_seq.diagram_classes.get(i).json_sequences.add(new_seq_to_class);
                                create_sequence(empty_seq, pane, 0, diagram);
                                return;
                            }
                        }
                    }
                }
            }
        });

        item1.setOnAction(e -> {
            FileChooser file_save = new FileChooser();
            file_save.setTitle("Save file");
            File file = file_save.showSaveDialog(seq_stage);
            if (file != null) {
                String seq_new_file_path = file.getPath();
                try {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    Writer writer = new FileWriter(seq_new_file_path);
                    writer.write(gson.toJson(empty_seq));
                    writer.close();
                } catch (Exception exception) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("Error during saving");
                    alert.showAndWait();
                }
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setHeaderText("Error during saving");
                errorAlert.showAndWait();
            }
        });
    }

    private void draw_sequence_screen(JsonDiagram seq_diagram, JsonDiagram diagram) {
        Group seq_root = new Group();
        Menu menu = new Menu("File");
        MenuItem item1 = new MenuItem("Save");

        Menu menu1 = new Menu("Edit");
        MenuItem item2 = new MenuItem("Add new object");
        MenuItem item3 = new MenuItem("Add new message");
        menu.getItems().addAll(item1);
        menu1.getItems().addAll(item2, item3);

        MenuBar menu_bar = new MenuBar(menu, menu1);
        seq_root.getChildren().add(menu_bar);
        Scene seq_scene = new Scene(seq_root, 750, 600);

        Pane pane = new Pane();
        pane.setLayoutY(25);
        pane.setPrefWidth(Region.USE_COMPUTED_SIZE);
        pane.setPrefHeight(Region.USE_COMPUTED_SIZE);

        //CALL FUNC WITH CREATE DIAGRAM
        create_sequence(seq_diagram, pane, 0, diagram);

        seq_root.getChildren().add(pane);

        Stage seq_stage = new Stage();
        seq_stage.setTitle("UML Editor");
        seq_stage.getIcons().add(new Image("C:\\Users\\lebed\\projects\\demo\\lib\\IMG_20200306_172134.jpg"));
        seq_stage.setScene(seq_scene);
        seq_stage.show();

        item2.setOnAction(e -> {
            TextInputDialog new_line_name = new TextInputDialog();
            new_line_name.setTitle("New object name");
            new_line_name.setHeaderText("Enter name for new object");
            Optional<String> result = new_line_name.showAndWait();
            if (result.isEmpty()) {
                return;
            }
            String name = new_line_name.getEditor().getText();
            String name_sec = "";
            double new_posX = 0;
            double offset = 0;
            for (int i = 0; i < seq_diagram.diagram_classes.size(); i++) {
                for (int j  = 0; j < seq_diagram.diagram_classes.get(i).json_sequences.size(); j++) {
                    if (seq_diagram.diagram_classes.get(i).json_sequences.get(j).send_class.class_name.equals(name)) {
                        Alert exists = new Alert(Alert.AlertType.ERROR);
                        exists.setHeaderText("Object with this name already exists, do you want change name?");
                        ButtonType yes = new ButtonType("Yes");
                        ButtonType no = new ButtonType("No");
                        exists.getButtonTypes().addAll(yes, no);
                        Optional<ButtonType> res = exists.showAndWait();
                        if (res.get().equals(yes)) {
                            TextInputDialog new_name_sec = new TextInputDialog();
                            new_line_name.setTitle("New object name");
                            new_line_name.setHeaderText("Enter name for new object");
                            new_line_name.showAndWait();
                            name_sec = new_line_name.getEditor().getText();
                        }
                        else if (res.get().equals(no)) {
                            return;
                        }
                    }
                    else {
                        new_posX = 250 + offset;
                    }
                    offset += 75;
                }
                if (!name_sec.equals("")) {
                    JsonSequence new_seq = new JsonSequence(name_sec, 300, new_posX);
                    seq_diagram.diagram_classes.get(i).json_sequences.add(new_seq);
                }
                else {
                    JsonSequence new_seq = new JsonSequence(name, 300, new_posX);
                    seq_diagram.diagram_classes.get(i).json_sequences.add(new_seq);
                }
            }
            create_sequence(seq_diagram, pane, 0, diagram);
        });

        item3.setOnAction(e -> {
            Dialog<Object> dialog = new Dialog<>();
            dialog.setTitle("Message settings");
            dialog.setHeaderText("Choose the communicating classes");
            dialog.setResizable(true);
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            ComboBox from_class = new ComboBox<>();
            ComboBox to_class = new ComboBox<>();
            TextField message = new TextField();
            for (int i = 0; i < seq_diagram.diagram_classes.size(); i++) {
                for (int j = 0; j < seq_diagram.diagram_classes.get(i).json_sequences.size(); j++){
                    if (!from_class.getItems().contains(seq_diagram.diagram_classes.get(i).json_sequences.get(j).send_class.class_name)) {
                        from_class.getItems().add(seq_diagram.diagram_classes.get(i).json_sequences.get(j).send_class.class_name);
                    }
                    if (!to_class.getItems().contains(seq_diagram.diagram_classes.get(i).json_sequences.get(j).send_class.class_name)) {
                        to_class.getItems().add(seq_diagram.diagram_classes.get(i).json_sequences.get(j).send_class.class_name);
                    }
                }
            }
            dialogPane.setContent(new VBox(8, from_class, to_class, message));
            dialog.showAndWait();
            int id_seq = 1;
            for (int i = 0; i < seq_diagram.diagram_classes.size(); i++) {
                for (int j = 0; j < seq_diagram.diagram_classes.get(i).json_sequences.size(); j++){
                    if (seq_diagram.diagram_classes.get(i).json_sequences.get(j).send_class.class_name.equals(from_class.getValue().toString())) {
                        for (int k = 0; k < seq_diagram.diagram_classes.get(i).json_sequences.size(); k++) {
                            if (seq_diagram.diagram_classes.get(i).json_sequences.get(k).involved_class.class_name.equals(to_class.getValue().toString())){
                                double posX = seq_diagram.diagram_classes.get(i).json_sequences.get(k).involved_class.positionX;
                                String seq_name = "New_user_seq" + (id_seq + 1);
                                JsonSequence new_seq_to_class = new JsonSequence(seq_name, seq_diagram.diagram_classes.get(i).json_sequences.get(j).send_class.class_name, seq_diagram.diagram_classes.get(i).json_sequences.get(k).involved_class.class_name, seq_diagram.diagram_classes.get(i).json_sequences.get(j).send_class.positionX, posX, 300, message.getText());
                                seq_diagram.diagram_classes.get(i).json_sequences.add(new_seq_to_class);
                                create_sequence(seq_diagram, pane, 0, diagram);
                                return;
                            }
                        }
                    }
                }
            }
        });

        item1.setOnAction(e -> {
            FileChooser file_save = new FileChooser();
            file_save.setTitle("Save file");
            File file = file_save.showSaveDialog(seq_stage);
            if (file != null) {
                String seq_new_file_path = file.getPath();
                try {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    Writer writer = new FileWriter(seq_new_file_path);
                    writer.write(gson.toJson(seq_diagram));
                    writer.close();
                } catch (Exception exception) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("Error during saving");
                    alert.showAndWait();
                }
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setHeaderText("Error during saving");
                errorAlert.showAndWait();
            }
        });
    }

    public void create_sequence(JsonDiagram seq_diagram, Pane root, double offset, JsonDiagram diagram) {
        root.getChildren().removeAll(root.getChildren());
        int arrow_offset = 0;
        for (int i = 0; i < seq_diagram.diagram_classes.size(); i++) {
            for (int j = 0; j < seq_diagram.diagram_classes.get(i).json_sequences.size(); j++) {
                // Life time line //
                VBox life_time = new VBox();
                life_time.setStyle("-fx-padding: 10;" +
                        "-fx-border-radius: 5;" +
                        "-fx-border-color: green;");
                life_time.setPrefWidth(10);
                life_time.setPrefHeight(seq_diagram.diagram_classes.get(i).json_sequences.get(j).lifetime);
                life_time.setLayoutX(seq_diagram.diagram_classes.get(i).json_sequences.get(j).send_class.positionX);
                life_time.setLayoutY(75);

                Label name = new Label(seq_diagram.diagram_classes.get(i).json_sequences.get(j).send_class.class_name);
                name.setLayoutX(seq_diagram.diagram_classes.get(i).json_sequences.get(j).send_class.positionX + 2);
                name.setLayoutY(50);
                name.setStyle("-fx-border-width: 1;"
                        + "-fx-border-radius: 1;"
                        + "-fx-border-color: black;");
                
                int finalI = i;
                int finalJ = j;
                int check = 0;
                for (int p = 0; p < diagram.diagram_classes.size(); p++) {
                    if (seq_diagram.diagram_classes.get(i).json_sequences.get(j).send_class.class_name.equals(diagram.diagram_classes.get(p).class_name)) {
                        name.setStyle("-fx-border-width: 1;"
                                + "-fx-border-radius: 1;"
                                + "-fx-border-color: green;");
                        check = 1;
                    }
                    else if (check != 1) {
                        name.setStyle("-fx-border-width: 1;"
                                + "-fx-border-radius: 1;"
                                + "-fx-border-color: red;");
                    }
                }

                name.setOnMouseClicked(e -> {
                    name.setText("");
                    TextField t_name = new TextField();
                    root.getChildren().add(t_name);
                    t_name.setOnKeyPressed(new EventHandler<KeyEvent>() {
                        @Override
                        public void handle(KeyEvent keyEvent) {
                            if (keyEvent.getCode().equals(KeyCode.ENTER)){
                                name.setText(t_name.getText());
                                seq_diagram.diagram_classes.get(finalI).json_sequences.get(finalJ).send_class.class_name = t_name.getText();
                            }
                            if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
                                root.getChildren().remove(t_name);
                            }
                        }
                    });
                });

                Label message = new Label();

                message.setOnMouseClicked(e -> {
                    message.setText("");
                    TextField t_message = new TextField();
                    root.getChildren().add(t_message);
                    t_message.setOnKeyPressed(new EventHandler<KeyEvent>() {
                        @Override
                        public void handle(KeyEvent keyEvent) {
                            if (keyEvent.getCode().equals(KeyCode.ENTER)){
                                message.setText(t_message.getText());
                                seq_diagram.diagram_classes.get(finalI).json_sequences.get(finalJ).msg = t_message.getText();
                            }
                            if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
                                root.getChildren().remove(t_message);
                            }
                        }
                    });
                });

                if (seq_diagram.diagram_classes.get(i).json_sequences.get(j).involved_class != null) {
                    if (seq_diagram.diagram_classes.get(i).json_sequences.get(j).send_class.positionX < seq_diagram.diagram_classes.get(i).json_sequences.get(j).involved_class.positionX) {
                        draw_arrow(seq_diagram.diagram_classes.get(i).json_sequences.get(j).send_class.positionX + 25, 90 + arrow_offset, seq_diagram.diagram_classes.get(i).json_sequences.get(j).involved_class.positionX, 90 + arrow_offset, root);
                        message.setText(seq_diagram.diagram_classes.get(i).json_sequences.get(j).msg);
                        message.setLayoutX(seq_diagram.diagram_classes.get(i).json_sequences.get(j).send_class.positionX + 25);
                        message.setLayoutY(90 + arrow_offset);
                    }
                    else if (seq_diagram.diagram_classes.get(i).json_sequences.get(j).send_class.positionX > seq_diagram.diagram_classes.get(i).json_sequences.get(j).involved_class.positionX){
                        draw_arrow(seq_diagram.diagram_classes.get(i).json_sequences.get(j).send_class.positionX, 90 + arrow_offset, seq_diagram.diagram_classes.get(i).json_sequences.get(j).involved_class.positionX + 25, 90 + arrow_offset, root);
                        message.setText(seq_diagram.diagram_classes.get(i).json_sequences.get(j).msg);
                        message.setLayoutX(seq_diagram.diagram_classes.get(i).json_sequences.get(j).send_class.positionX - 105);
                        message.setLayoutY(90 + arrow_offset);
                    }
                }
                int check1 = 0;

                for (int p = 0; p < diagram.diagram_classes.size(); p++) {
                    for (int k = 0; k < diagram.diagram_classes.get(p).operations.size(); k++) {
                        if (diagram.diagram_classes.get(p).operations.get(k).getOp_name().equals(seq_diagram.diagram_classes.get(i).json_sequences.get(j).msg)) {
                            message.setStyle("-fx-text-fill: green;");
                            check1 = 1;
                        }
                        else if (check1 != 1) {
                            message.setStyle("-fx-text-fill: red;");
                        }
                    }
                }
                root.getChildren().addAll(life_time, name, message);
                arrow_offset += 30;
            }
        }
    }

    private void create_classes(JsonDiagram diagram, Pane root, int delay, int delay_sec){
        root.getChildren().removeAll(root.getChildren());
        for (int i = 0; i < diagram.diagram_classes.size();i++) {
            VBox box = new VBox();
            box.setStyle("-fx-padding: 10;" +
                    "-fx-border-style: solid inside;" +
                    "-fx-border-width: 2;" +
                    "-fx-border-insets: 5;" +
                    "-fx-border-radius: 5;" +
                    "-fx-border-color: green;");    //barva hranic diagramu

            //*** Class name ***//
            Label t = new Label("Class name: " + diagram.diagram_classes.get(i).class_name);
            Separator separator = new Separator(Orientation.HORIZONTAL);
            separator.setStyle("-fx-background-color: #000000;" + "-fx-background-radius: 1");//čára pod Class name
            box.getChildren().add(t);
            box.getChildren().add(separator);
            int finalI = i;
            t.setOnMouseClicked(e -> {
                t.setText("Class name: ");
                TextField name = new TextField();
                box.getChildren().add(name);
                name.setOnKeyPressed(new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent keyEvent) {
                        if (keyEvent.getCode().equals(KeyCode.ENTER)){
                            String new_name = name.getText();
                            t.setText("Class name: " + new_name);
                            diagram.diagram_classes.get(finalI).class_name = new_name;
                        }
                        if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
                            box.getChildren().remove(name);
                        }
                    }
                });
            });
            //*** End of edit Class name ***//

            //****** Attribute's change ******//
            Label attrib = new Label("Attributes:\n" + diagram.diagram_classes.get(i).getAttributes() + "\n");
            int finalI1 = i;
            // Na tohle by se mega hodila nejaka funkce :pepela: //
            // Ja vim :kekw: //
            attrib.setOnMouseClicked(e -> {
                Alert choice = new Alert(Alert.AlertType.CONFIRMATION);
                choice.setTitle("Choose, please, next step");
                choice.setHeaderText("Do you want to remove first attribute or add new one attribute?" +
                        "For confirmation of remove/new name, please, press enter");
                ButtonType remove_bttn = new ButtonType("Remove first");
                ButtonType add_bttn = new ButtonType("Add new");
                ButtonType cancel_bttn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);//cancel button
                choice.getButtonTypes().setAll(remove_bttn, add_bttn, cancel_bttn);
                Optional<ButtonType> result = choice.showAndWait();
                TextField name = new TextField();
                box.getChildren().add(name);
                //****** CANCEL BUTTON *******//
                if (result.get() == cancel_bttn) {
                    box.getChildren().remove(name);}
                //*** END CANCEL BUTTON ***//
                name.setOnKeyPressed(new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent keyEvent) {
                        if (result.get() == remove_bttn) {
                            diagram.diagram_classes.get(finalI1).removeAllAttributes();
                            if (keyEvent.getCode().equals(KeyCode.ENTER)){
                                attrib.setText("Attributes: " + diagram.diagram_classes.get(finalI1).getAttributes());
                            }
                        }
                        else if (result.get() == add_bttn) {
                            if (keyEvent.getCode().equals(KeyCode.ENTER)){
                                String new_name = name.getText();
                                String old_attr = attrib.getText();
                                attrib.setText(old_attr + new_name);
                                String[] token = new_name.split(":");
                                UMLAttribute attr = new UMLAttribute(token[0], token[1]);
                                diagram.diagram_classes.get(finalI).addAttribute(attr);
                            }
                        }
                        if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
                            box.getChildren().remove(name);
                        }
                    }
                });
            });
            box.getChildren().add(attrib);
            //**** End of Attribute's change ****//

            //**** Operation's change *****//
            Label operations = new Label("Operations: " + diagram.diagram_classes.get(i).getArguments());
            for (int j = 0; j < diagram.diagram_classes.get(i).operations.size(); j++) {
                Label name = new Label(diagram.diagram_classes.get(i).operations.get(j).op_name);
                Label input  = new Label(diagram.diagram_classes.get(i).operations.get(j).input_params.toString());
                Label return_t = new Label(diagram.diagram_classes.get(i).operations.get(j).return_type);
                operations.setText("Operations:\n" + name.getText() + "\n" + "input: " + input.getText() + " return: " + return_t.getText());
                int finalJ = j;
                int finalI2 = i;
                operations.setOnMouseClicked(e -> {
                    Alert choice = new Alert(Alert.AlertType.CONFIRMATION);
                    choice.setTitle("Choose, please, next step");
                    choice.setHeaderText("Choose the field to change: name, input, return");
                    ButtonType change_name_bttn = new ButtonType("name");
                    ButtonType change_input_bttn = new ButtonType("input");
                    ButtonType change_return_bttn = new ButtonType("return");
                    choice.getButtonTypes().addAll(change_name_bttn, change_input_bttn, change_return_bttn);
                    Optional<ButtonType> result = choice.showAndWait();
                    if (result.get() == change_name_bttn) {
                        TextField for_name = new TextField();
                        box.getChildren().add(for_name);
                        for_name.setOnKeyPressed(new EventHandler<KeyEvent>() {
                            @Override
                            public void handle(KeyEvent keyEvent) {
                                if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                                    String new_name = for_name.getText();
                                    name.setText(new_name);
                                    diagram.diagram_classes.get(finalI2).operations.get(finalJ).op_name = new_name;
                                    operations.setText("Operations:\n" + name.getText() + "\n" + "input: " + input.getText() + " return: " + return_t.getText());
                                }
                                if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
                                    box.getChildren().remove(for_name);
                                }
                            }
                        });
                    } else if (result.get() == change_input_bttn) {
                        TextField for_input = new TextField();
                        box.getChildren().add(for_input);
                        for_input.setOnKeyPressed(new EventHandler<KeyEvent>() {
                            @Override
                            public void handle(KeyEvent keyEvent) {
                                if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                                    String new_input = for_input.getText();
                                    input.setText(new_input);
                                    if (diagram.diagram_classes.get(finalI2).operations.get(finalJ).input_params.size() != 0) {
                                        diagram.diagram_classes.get(finalI2).operations.get(finalJ).input_params.remove(finalJ);
                                    }
                                    diagram.diagram_classes.get(finalI2).operations.get(finalJ).input_params.add(new_input);
                                    operations.setText("Operations:\n" + name.getText() + "\n" + "input: " + input.getText() + " return: " + return_t.getText());
                                }
                                if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
                                    box.getChildren().remove(for_input);
                                }
                            }
                        });
                    } else if (result.get() == change_return_bttn) {
                        TextField for_return = new TextField();
                        box.getChildren().add(for_return);
                        for_return.setOnKeyPressed(new EventHandler<KeyEvent>() {
                            @Override
                            public void handle(KeyEvent keyEvent) {
                                if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                                    String new_return = for_return.getText();
                                    return_t.setText(new_return);
                                    diagram.diagram_classes.get(finalI2).operations.get(finalJ).return_type = new_return;
                                    operations.setText("Operations:\n" + name.getText() + "\n" + "input: " + input.getText() + " return: " + return_t.getText());
                                }
                                if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
                                    box.getChildren().remove(for_return);
                                }
                            }
                        });
                    }
                });
            }
            //**** End of operation's change ****//

            Separator separator1 = new Separator(Orientation.HORIZONTAL);
            separator1.setStyle("-fx-background-color: #000000;" + "-fx-background-radius: 1");//barva pod atributy
            box.getChildren().add(separator1);
            box.getChildren().add(operations);
            box.setLayoutX(diagram.diagram_classes.get(i).positionX);
            box.setOnMouseEntered(e -> {
                box.requestFocus();
            });
            box.setOnMousePressed( e-> {
                startDragX = e.getSceneX();
                startDragY = e.getSceneY();
                if (dragged) {
                    startDragY = lastDragY;
                    startDragX = lastDragX;
                }
            });
            box.setOnMouseDragged(e -> {
                box.setTranslateX(e.getSceneX() - startDragX);
                lastDragX = box.snapPositionX(startDragX);
                box.setLayoutX(lastDragX);
                box.setTranslateY(e.getSceneY() - startDragY);
                lastDragY = box.snapPositionY(startDragY);
                box.setLayoutY(lastDragY);
                dragged = true;
            });
            box.backgroundProperty().bind(Bindings
                    .when (box.focusedProperty())
                    .then (focusBackground)
                    .otherwise (unfocusBackground)
            );
            if (diagram.diagram_classes.get(i).positionX == 0 || diagram.diagram_classes.get(i).positionY == 0) {
                if (i == 0) {
                    box.setLayoutX(10);
                    box.setLayoutY(50);
                }
                else {
                    box.setLayoutX(diagram.diagram_classes.get(i - 1).positionX + delay);
                    box.setLayoutY(diagram.diagram_classes.get(i - 1).positionY);
                }
            }
            if (10 + delay > 550) {
                box.setLayoutX(10 + delay_sec);
                box.setLayoutY(300);
                delay_sec += 250;
            }
            else {
                box.setLayoutY(diagram.diagram_classes.get(i).positionY);
            }
            if (i != 0) {
                draw_arrow(box.getLayoutX(), box.getLayoutY(), diagram.diagram_classes.get(i - 1).positionX,diagram.diagram_classes.get(i -1).positionY + root.getLayoutY(), root);
            }
            root.getChildren().add(box);
            delay = delay + 250;
        }
    }

    public void draw_arrow (double startX, double startY, double endX, double endY, Pane pane) {
        double slope = (startY - endY) / (startX - endX);
        double lineAngle = Math.atan(slope);

        double arrowAngle = startX > endX ? Math.toRadians(45) : -Math.toRadians(225);

        Line line = new Line(startX, startY, endX, endY);

        double lineLength = Math.sqrt(Math.pow(startX - endX, 2) + Math.pow(startY - endY, 2));
        double arrowLength = lineLength / 10;

        // create the arrow legs
        Line arrow1 = new Line();
        arrow1.setStartX(line.getEndX());
        arrow1.setStartY(line.getEndY());
        arrow1.setEndX(line.getEndX() + arrowLength * Math.cos(lineAngle - arrowAngle));
        arrow1.setEndY(line.getEndY() + arrowLength * Math.sin(lineAngle - arrowAngle));

        Line arrow2 = new Line();
        arrow2.setStartX(line.getEndX());
        arrow2.setStartY(line.getEndY());
        arrow2.setEndX(line.getEndX() + arrowLength * Math.cos(lineAngle + arrowAngle));
        arrow2.setEndY(line.getEndY() + arrowLength * Math.sin(lineAngle + arrowAngle));
        pane.getChildren().addAll(line, arrow1, arrow2);
    }
}

