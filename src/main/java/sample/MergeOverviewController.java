package sample;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Route;

import java.io.Serializable;

public class MergeOverviewController {

    public MergeOverviewController()
    {

    }

    @FXML
    TableView<Serializable> objectTable;
    @FXML
    TableColumn<Serializable, String> objectColumn;
    @FXML
    TableColumn<Serializable, Class>  type;

    Controller controller = Controller.getInstance();

    public void initialize(){


        objectColumn.setCellValueFactory( new PropertyValueFactory<>( "number" ) );
        type.setCellValueFactory( new PropertyValueFactory<>( this.getClass().toString() ) );
        objectTable.setItems(controller.getMergeElements());
        objectTable.refresh();
    }


}
