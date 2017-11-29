package sample;

import javafx.fxml.Initializable;
import javafx.stage.Stage;
import model.DataModel;

import java.net.URL;
import java.util.ResourceBundle;

public class EditRouteWindowController implements Initializable{

    private DataModel dataModel = DataModel.getInstance();
    private MainWindowController mainWindowController;
    private Stage                thisWindow;

    public EditRouteWindowController( MainWindowController mainWindowController , Stage thisWindow ){
        this.mainWindowController = mainWindowController;
        this.thisWindow = thisWindow;
    }

    @Override
    public void initialize( URL location , ResourceBundle resources ){

    }
}
