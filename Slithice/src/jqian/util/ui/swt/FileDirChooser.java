package jqian.util.ui.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class FileDirChooser {
  Display display = new Display();
  Shell shell = new Shell(display);
  
  // the label used to display selected dir/file.
  Label label;
  
  Button buttonSelectDir;
  Button buttonSelectFile;
  
  String selectedDir;
  String fileFilterPath = "C:/";

  public FileDirChooser() {
    label = new Label(shell, SWT.BORDER | SWT.WRAP);
    label.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
    label.setText("Select a dir/file by clicking the buttons below.");
    
    buttonSelectDir = new Button(shell, SWT.PUSH);
    buttonSelectDir.setText("Select a directory");
    buttonSelectDir.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        DirectoryDialog directoryDialog = new DirectoryDialog(shell);
        
        directoryDialog.setFilterPath(selectedDir);
        directoryDialog.setMessage("Please select a directory and click OK");
        
        String dir = directoryDialog.open();
        if(dir != null) {
          label.setText("Selected dir: " + dir);
          selectedDir = dir;
        }
      }
    });
    
    buttonSelectFile = new Button(shell, SWT.PUSH);
    buttonSelectFile.setText("Select a file/multiple files");
    buttonSelectFile.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        FileDialog fileDialog = new FileDialog(shell, SWT.MULTI);

        fileDialog.setFilterPath(fileFilterPath);
        
        fileDialog.setFilterExtensions(new String[]{"*.rtf", "*.html", "*.*"});
        fileDialog.setFilterNames(new String[]{ "Rich Text Format", "HTML Document", "Any"});
        
        String firstFile = fileDialog.open();

        if(firstFile != null) {
          fileFilterPath = fileDialog.getFilterPath();
          String[] selectedFiles = fileDialog.getFileNames();
          StringBuffer sb = new StringBuffer("Selected files under dir " + fileDialog.getFilterPath() +  ": \n");
          for(int i=0; i<selectedFiles.length; i++) {
            sb.append(selectedFiles[i] + "\n");
          }
          label.setText(sb.toString());
        }
      }
    });
    
    label.setBounds(0, 0, 400, 60);
    buttonSelectDir.setBounds(0, 65, 200, 30);
    buttonSelectFile.setBounds(200, 65, 200, 30);

    shell.pack();
    shell.open();
    //textUser.forceFocus();

    // Set up the event loop.
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        // If no more entries in event queue
        display.sleep();
      }
    }

    display.dispose();
  }


  public static void main(String[] args) {
    new FileDirChooser();
  }
}

