package cavendish.jetty.consumer;

import java.util.function.Consumer;

import org.apache.jena.atlas.lib.Sink;

import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.modify.request.UpdateAdd;
import com.hp.hpl.jena.sparql.modify.request.UpdateClear;
import com.hp.hpl.jena.sparql.modify.request.UpdateCopy;
import com.hp.hpl.jena.sparql.modify.request.UpdateCreate;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataDelete;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataInsert;
import com.hp.hpl.jena.sparql.modify.request.UpdateDeleteWhere;
import com.hp.hpl.jena.sparql.modify.request.UpdateDrop;
import com.hp.hpl.jena.sparql.modify.request.UpdateLoad;
import com.hp.hpl.jena.sparql.modify.request.UpdateModify;
import com.hp.hpl.jena.sparql.modify.request.UpdateMove;
import com.hp.hpl.jena.sparql.modify.request.UpdateVisitor;
import com.hp.hpl.jena.update.Update;

public class UpdateConsumer implements Consumer<Update>, UpdateVisitor {

  @Override
  public void accept(Update t) {
    t.visit(this);    
  }

  @Override
  public void visit(UpdateDrop update) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(UpdateClear update) {
  }

  @Override
  public void visit(UpdateCreate update) {
  }

  @Override
  public void visit(UpdateLoad update) {
  }

  @Override
  public void visit(UpdateAdd update) {
  }

  @Override
  public void visit(UpdateCopy update) {
  }

  @Override
  public void visit(UpdateMove update) {
  }

  @Override
  public void visit(UpdateDataInsert update) {
  }

  @Override
  public void visit(UpdateDataDelete update) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void visit(UpdateDeleteWhere update) {
  }

  @Override
  public void visit(UpdateModify update) {
  }

  @Override
  public Sink<Quad> createInsertDataSink() {
    return null;
  }

  @Override
  public Sink<Quad> createDeleteDataSink() {
    return null;
  }

}
