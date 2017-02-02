package org.avario.ui.prefs;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.avario.R;
import org.avario.engine.datastore.DataAccessObject;
import org.avario.utils.StringFormatter;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.content.Context;
import android.hardware.SensorManager;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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


public class EditQNHValue extends EditTextPreference {

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
		String pressao = getMetar("SGBL");
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

	private String getMetar (String estacao)
	{
		String pressao = "1013.25";

		String address = String.format("http://servicos.cptec.inpe.br/XML/estacao/%s/condicoesAtuais.xml", estacao);

		HttpGet uri = new HttpGet(address);

		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse resp = null;
		try {
			resp = client.execute(uri);
		} catch (IOException e) {
			e.printStackTrace();
		}

		StatusLine status = resp.getStatusLine();
		if (status.getStatusCode() != 200) {
			Log.d("xxx", "HTTP error, invalid server status code: " + resp.getStatusLine());
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(resp.getEntity().getContent());

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return pressao;
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
