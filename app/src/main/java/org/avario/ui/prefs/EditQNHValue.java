package org.avario.ui.prefs;

import android.content.Context;
import android.hardware.SensorManager;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.avario.R;
import org.avario.engine.datastore.DataAccessObject;
import org.avario.utils.StringFormatter;


/*
class metar {
	private String codigo;
	private String atualizacao;
	private String pressao;
	private String temperatura;
	private String tempo;
	private String tempo_desc;
	private String umidade;
	private String vento_dir;
	private String vento_int;
	private String visibilidade;

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getAtualizacao() {
		return atualizacao;
	}

	public void setAtualizacao(String atualizacao) {
		this.atualizacao = atualizacao;
	}

	public String getPressao() {
		return pressao;
	}

	public void setPressao(String pressao) {
		this.pressao = pressao;
	}

	public String getTemperatura() {
		return temperatura;
	}

	public void setTemperatura(String temperatura) {
		this.temperatura = temperatura;
	}

	public String getTempo() {
		return tempo;
	}

	public void setTempo(String tempo) {
		this.tempo = tempo;
	}

	public String getTempo_desc() {
		return tempo_desc;
	}

	public void setTempo_desc(String tempo_desc) {
		this.tempo_desc = tempo_desc;
	}

	public String getUmidade() {
		return umidade;
	}

	public void setUmidade(String umidade) {
		this.umidade = umidade;
	}

	public String getVento_dir() {
		return vento_dir;
	}

	public void setVento_dir(String vento_dir) {
		this.vento_dir = vento_dir;
	}

	public String getVento_int() {
		return vento_int;
	}

	public void setVento_int(String vento_int) {
		this.vento_int = vento_int;
	}

	public String getVisibilidade() {
		return visibilidade;
	}

	public void setVisibilidade(String visibilidade) {
		this.visibilidade = visibilidade;
	}


}
*/


public class EditQNHValue extends EditTextPreference {

	//private static final String URL = "http://www.nasa.gov/rss/dyn/lg_image_of_the_day.rss";
	//private static final int RSS_DOWNLOAD_REQUEST_CODE = 0;

	protected TextView mValueText;

	public EditQNHValue(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayoutResource(R.layout.preference_with_value);
	}

	public EditQNHValue(Context context) {
		super(context);
		setLayoutResource(R.layout.preference_with_value);
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		mValueText = (TextView) view.findViewById(R.id.preference_value);
		if (mValueText != null) {
			mValueText.setText(getText());
		}
		this.setDialogMessage(R.string.ref_alt_description);
		this.setDialogTitle(R.string.ref_alt_title);
	}

	@Override
	public void setText(String text) {
		if (mValueText != null) {
			try {
				int newAltitude = Integer.valueOf(text);
				text = computeQNHFromAltitude(newAltitude);
				mValueText.setText(text);
			} catch (Exception e) {
				super.setText("1013.25");
			}
		}
		super.setText(text);
	}

	private String computeQNHFromAltitude(int newAltitude) {
		float ref = SensorManager.PRESSURE_STANDARD_ATMOSPHERE + 3000;
		// double h = location.getAltitude();
		// adjust the reference pressure until the pressure sensor
		// altitude match the gps altitude +-2m
		float lastPresure = DataAccessObject.get().getLastPresure();
		if (lastPresure > 0 && newAltitude > 0 && newAltitude < 10000) {
			double delta = Math.abs(SensorManager.getAltitude(ref, lastPresure) - newAltitude);
			while (delta > 2 && ref > 0) {
				ref -= 0.1 * delta;
				delta = Math.abs(SensorManager.getAltitude(ref, lastPresure) - newAltitude);
			}
		} else {
			ref = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;
		}
		return String.valueOf(ref);
	}

	@Override
	protected void onAddEditTextToDialogView(View dialogView, EditText editText) {
		// TODO Auto-generated method stub
		editText.setText(StringFormatter.noDecimals(DataAccessObject.get().getLastAltitude()));
		super.onAddEditTextToDialogView(dialogView, editText);
	}
}
