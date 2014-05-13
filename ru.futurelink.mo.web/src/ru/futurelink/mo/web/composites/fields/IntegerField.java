/**
 * 
 */
package ru.futurelink.mo.web.composites.fields;

import org.eclipse.rap.rwt.scripting.ClientListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;

import ru.futurelink.mo.orm.dto.FilterDTO;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.CommonItemComposite;
import ru.futurelink.mo.web.controller.CompositeParams;

/**
 * @author pavlov
 *
 */
public class IntegerField extends TextField {

	private static String digitsOnlyJS = 
			"var handleEvent = function( event ) {\n"
			+ "	var regexp = /^[0-9]*$/;\n"
			+ "	if( event.text.match( regexp ) === null ) {\n"
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
	public IntegerField(ApplicationSession session, CommonComposite parent,
			int style, CompositeParams params, CommonItemComposite c) {
		super(session, parent, style, params, c);		
	}

	public IntegerField(ApplicationSession session, CommonComposite parent,
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

	            // Проверяем текст на соотстветствие подсказке
	            // если текст - подсказка, то разрешает ее.
	            if (newS != null && newS.equals(getHint())) {
	            	e.doit = true;
	            	return;
	            }

	            boolean isInteger = true;
	            // Если число или пустая строка - разрешаем!
	            if (!newS.equals("")) {
	            	try {
	            		Integer.parseInt(newS);
	            	} catch(NumberFormatException ex) {
	            		isInteger = false;
	            	}
	            }

	            if(!isInteger) e.doit = false;				
			}
		});*/	
	}
	
	@Override
	public Object getValue() {
		if (getText() == null || getText().equals("")) return (Integer)null;
		return Integer.valueOf(getText());
	}
	
	@Override
	public boolean isEmpty() {
		return (((Text)mControl).getText() == null) || ((Text)mControl).getText().isEmpty() || 
				(Integer.valueOf(((Text)mControl).getText()) == 0);
	}
}
