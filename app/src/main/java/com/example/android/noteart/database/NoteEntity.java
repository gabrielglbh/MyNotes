package com.example.android.noteart.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

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

    @Ignore
    public NoteEntity(String title, String description, String checkbox, Date lastUpdated, int archivada, int esChecklist) {
        setTitulo(title);
        setDescripcion(description);
        setCheckbox(checkbox);
        setFecha(lastUpdated);
        setArchivada(archivada);
        setEsChecklist(esChecklist);
    }

    public NoteEntity(int id, String titulo, String descripcion, String checkbox, Date fecha, int archivada, int esChecklist) {
        setId(id);
        setTitulo(titulo);
        setDescripcion(descripcion);
        setCheckbox(checkbox);
        setFecha(fecha);
        setArchivada(archivada);
        setEsChecklist(esChecklist);
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
}
