package pdf_utilities;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Controller
{
    public static final int INITIAL_FIELDS = 4;

    private ArrayList<TextField> textFields = new ArrayList<>();
    private ArrayList<Button> addButtons = new ArrayList<>();
    private HashMap<Integer, File> filesToMerge = new HashMap<>();

    @FXML
    private VBox fieldsContainer;

    @FXML
    public void initialize()
    {
        for (int i = 0; i < INITIAL_FIELDS; i++)
        {
            addFileField();
        }
    }

    /**
     * Adds a new field for opening a file
     */
    private void addFileField()
    {
        int fieldIndex = textFields.size();

        // Align all elements relating to one file horizontally with HBox
        HBox hBox = new HBox();
        hBox.setPadding(new Insets(0, 0, 10, 0));
        hBox.setSpacing(10);

        // Top element has padding towards the top and is moved to the right to align with other fields
        if (fieldIndex == 0)
        {
            hBox.setPadding(new Insets(10, 0, 10, 45));
        }

        // Textfield for displaying the file name
        TextField textField = new TextField("No file chosen");
        textField.setId("textField" + fieldIndex);
        textFields.add(textField);

        // Button to add more PDF files
        Image imagePDF = new Image(getClass().getResourceAsStream("file-pdf.png"));
        Button addFileButton = new Button("Open file...");
        addFileButton.setId("openFileButton" + fieldIndex);
        addFileButton.setOnAction(this::addFile);
        addFileButton.setGraphic(new ImageView(imagePDF));
        addButtons.add(addFileButton);

        // Add textfield and button to HBox
        hBox.getChildren().addAll(textField, addFileButton);
        hBox.setAlignment(Pos.CENTER);

        // Add a button to rearrange the order for all but the top field
        if (fieldIndex > 0)
        {
            Image imageUp = new Image(getClass().getResourceAsStream("up-arrow.png"));
            Button orderButton = new Button();
            orderButton.setId("orderButton" + fieldIndex);
            orderButton.setOnAction(this::reorder);
            orderButton.setGraphic(new ImageView(imageUp));

            hBox.getChildren().add(0, orderButton);
        }

        // Add HBox to surrounding pane
        fieldsContainer.getChildren().add(fieldIndex, hBox);
    }

    /**
     * Adds a new file
     */
    @FXML
    private void addFile(ActionEvent actionEvent)
    {
        Button button = (Button) actionEvent.getSource();
        int fileIndex = Integer.parseInt(button.getId().substring("openFileButton".length()));

        // Customize and open the filechooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose PDF");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(FileSystemView.getFileSystemView().getDefaultDirectory());
        List<File> files = fileChooser.showOpenMultipleDialog(fieldsContainer.getScene().getWindow());

        // Add chosen files to a map
        if (files != null)
        {
            int i = fileIndex;
            for (File file : files)
            {
                if (i >= textFields.size()) addFileField();
                textFields.get(i).setText(file.getName());
                filesToMerge.put(i, file);
                i++;
            }
        } else
        {
            textFields.get(fileIndex).setText("No file chosen");
            filesToMerge.remove(fileIndex);
        }
    }

    /**
     * Called by the "More files..." button
     * Adds a new field for opening a file
     */
    @FXML
    private void addField(ActionEvent actionEvent)
    {
        addFileField();
    }

    /**
     * Merge the PDF files in the correct order
     */
    @FXML
    private void merge(ActionEvent actionEvent)
    {
        // Customize filechooser
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(FileSystemView.getFileSystemView().getDefaultDirectory());
        fileChooser.setInitialFileName("merged-document.pdf");

        //Show save file dialog
        File fileToSave = fileChooser.showSaveDialog(fieldsContainer.getScene().getWindow());
        if (fileToSave == null) return; // User exited the dialog

        // Merge files
        PDFMergerUtility mergerUtility = new PDFMergerUtility();
        for (File file : filesToMerge.values())
        {
            if (file == null) break;
            try
            {
                mergerUtility.addSource(file);
            } catch (FileNotFoundException e)
            {
                // TODO: Handle file not found
                e.printStackTrace();
            }
        }
        mergerUtility.setDestinationFileName(fileToSave.getAbsolutePath());
        try
        {
            mergerUtility.mergeDocuments(null);
        } catch (IOException e)
        {
            // TODO: Do something here
            e.printStackTrace();
        }
    }

    /**
     * Called by the Up arrow button.
     * Rearranges the order of the PDF files
     */
    private void reorder(ActionEvent actionEvent)
    {
        Button button = (Button) actionEvent.getSource();
        int fileIndex = Integer.parseInt(button.getId().substring("orderButton".length()));

        // Temporary store the file above
        File file = filesToMerge.get(fileIndex - 1);
        String fileName = textFields.get(fileIndex - 1).getText();

        // Replace the file above
        filesToMerge.put(fileIndex - 1, filesToMerge.get(fileIndex));
        textFields.get(fileIndex - 1).setText(textFields.get(fileIndex).getText());

        // Replace file on same line
        filesToMerge.put(fileIndex, file);
        textFields.get(fileIndex).setText(fileName);
    }

}
