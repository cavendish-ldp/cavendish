package cavendish.blazegraph.task;

import com.bigdata.rdf.store.AbstractTripleStore;
import com.bigdata.rdf.task.AbstractApiTask;

public class IsQuadsTask extends AbstractApiTask<Boolean> {

  public IsQuadsTask(String namespace) {
    super(namespace, System.currentTimeMillis(), false);
  }

  @Override
  public Boolean call() throws Exception {
    AbstractTripleStore store = this.getQueryConnection().getTripleStore();
    try {
      return store.isQuads();
    } finally {
      store.close();
    }
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

}
