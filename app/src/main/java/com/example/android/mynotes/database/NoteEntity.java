package com.example.android.mynotes.database;

import java.util.Date;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "notas")
public class NoteEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String titulo;
    private String descripcion;
    private String checkbox;
    private int esChecklist;
    private Date fecha;
    private int archivada;
    private String tag;
    private int recordatorio;
    private String hora_recordatorio;
    private String fecha_recordatorio;

    @Ignore
    public NoteEntity(String title, String description, String checkbox, Date lastUpdated,
                      int archivada, int esChecklist, String tag, int recordatorio,
                      String hora_recordatorio, String fecha_recordatorio) {
        setTitulo(title);
        setDescripcion(description);
        setCheckbox(checkbox);
        setFecha(lastUpdated);
        setArchivada(archivada);
        setEsChecklist(esChecklist);
        setTag(tag);
        setRecordatorio(recordatorio);
        setFecha_recordatorio(fecha_recordatorio);
        setHora_recordatorio(hora_recordatorio);
    }

    public NoteEntity(int id, String titulo, String descripcion, String checkbox, Date fecha,
                      int archivada, int esChecklist, String tag, int recordatorio,
                      String hora_recordatorio, String fecha_recordatorio) {
        setId(id);
        setTitulo(titulo);
        setDescripcion(descripcion);
        setCheckbox(checkbox);
        setFecha(fecha);
        setArchivada(archivada);
        setEsChecklist(esChecklist);
        setTag(tag);
        setRecordatorio(recordatorio);
        setFecha_recordatorio(fecha_recordatorio);
        setHora_recordatorio(hora_recordatorio);
    }

    public void setId(int id) { this.id = id; }

    public int getId() { return this.id; }

    private void setTitulo(String title) { this.titulo = title; }

    public String getTitulo() { return this.titulo; }

    private void setDescripcion(String description) { this.descripcion = description; }

    public String getDescripcion() { return this.descripcion; }

    private void setCheckbox(String checkbox) {
        this.checkbox = checkbox;
    }

    public String getCheckbox() {
        return checkbox;
    }

    private void setFecha(Date lastUpdated) { this.fecha = lastUpdated; }

    public Date getFecha() { return this.fecha; }

    private void setArchivada(int archivada) { this.archivada = archivada; }

    public int getArchivada() { return this.archivada; }

    public int getEsChecklist() {
        return esChecklist;
    }

    private void setEsChecklist(int esChecklist) {
        this.esChecklist = esChecklist;
    }

    public String getTag() { return tag; }

    public void setTag(String tag) { this.tag = tag; }

    public int getRecordatorio() { return recordatorio; }

    public void setRecordatorio(int recordatorio) { this.recordatorio = recordatorio; }

    public String getFecha_recordatorio() { return fecha_recordatorio; }

    public void setFecha_recordatorio(String fecha_recordatorio) { this.fecha_recordatorio = fecha_recordatorio; }

    public String getHora_recordatorio() { return hora_recordatorio; }

    public void setHora_recordatorio(String hora_recordatorio) { this.hora_recordatorio = hora_recordatorio; }
}
