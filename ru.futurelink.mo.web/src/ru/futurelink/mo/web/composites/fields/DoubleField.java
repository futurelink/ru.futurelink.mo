package ru.futurelink.mo.web.composites.fields;

import org.eclipse.rap.rwt.scripting.ClientListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;

import ru.futurelink.mo.orm.dto.FilterDTO;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.CommonItemComposite;
import ru.futurelink.mo.web.controller.CompositeParams;

public class DoubleField extends TextField {

	private static String digitsOnlyJS = 
			"var handleEvent = function( event ) {\n"
			+ "	var regexp = /(^[0-9]*$)|(^[0-9]+\\.[0-9]*$)/;\n"
			+ "	var sourceText = event.widget.getText();\n"					
			+ "	var str1 = sourceText.substring(0, event.start);\n"
			+ "	var str2 = sourceText.substring(event.start, sourceText.length);\n"
			+ "	var destText = str1+event.text+str2;\n"			
			+ "	if( destText.match(regexp) === null ) {\n"
			+ "		event.doit = false;\n"
			+ "	}\n"
			+ "};\n"; 

	/**
	 * @param session
	 * @param parent
	 * @param style
	 * @param params
	 * @param c
	 */
	public DoubleField(ApplicationSession session, CommonComposite parent,
			int style, CompositeParams params, CommonItemComposite c) {
		super(session, parent, style, params, c);		
	}

	public DoubleField(ApplicationSession session, CommonComposite parent,
			int style, CompositeParams params, FilterDTO dto) {
		super(session, parent, style, params, dto);		
	}
	
	@Override
	protected void createControls(int style) {
		super.createControls(style);

		((Text)mControl).setText("");
		
		ClientListener clientListener = new ClientListener(digitsOnlyJS);
		mControl.addListener(SWT.Verify, clientListener);
		
		// Добавим листенер для того, чтобы можно было вводить только цифры с плавающей точкой
		/*((Text)mControl).addVerifyListener(new VerifyListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void verifyText(VerifyEvent e) {
	            Text text = (Text)e.getSource();
	            final String oldS = text.getText();
	            String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);

	            boolean isDouble = true;
	            try {
	                Double.parseDouble(newS);
	            } catch(NumberFormatException ex) {
	                isDouble = false;
	            }

	            if(!isDouble) e.doit = false;				
			}
		});*/
	}

	@Override
	public Object getValue() {
		if (getText() == null || getText().equals("")) return null;
		return Double.valueOf(getText());
	}

	@Override
	public boolean isEmpty() {
		return (((Text)mControl).getText() == null) || 
				((Text)mControl).getText().isEmpty() ||
				(Double.valueOf(((Text)mControl).getText()) == 0);
	}
}
