package edu.uvg.gt.Almazhen

class ExecutionResult
case class AffectedRows(n: Int) extends ExecutionResult{
  override def toString = "Affected " + n + " rows"
}

case class Error(msg: String) extends ExecutionResult{
  override def toString = msg
}

object Executor {
	final def DATABASE_DOES_NOT_EXIST(dbname: String) = Error(s"Database $dbname doesn't exist")
	final def DATABASE_ALREADY_EXISTS(dbname: String) = Error(s"Database $dbname exists")
	final def TABLE_ALREADY_EXISTS(dbname: String) = Error(s"Table $dbname exists")
	final def TABLE_DOES_NOT_EXISTS(dbname: String) = Error(s"Table $dbname doesn't exists")
	final def DATABASE_NOT_SELECTED = Error(s"No database is selected")
	final def THE_IMPOSSIBLE_HAPPENED(s: String) = Error(s"The impossible happened at $s")
	
	def exec(cmd: Command):ExecutionResult = cmd match {
	  case CreateDatabase(dbname) => {
	    Databases.create(dbname).fold[ExecutionResult](DATABASE_ALREADY_EXISTS(dbname))(_ => AffectedRows(1))
	  }
	  
	  case CreateTable(tbName, columns, constraints) => {
	    val maybeTable = Tables.create(tbName, columns, constraints)
	    maybeTable match {
	      case Left(e) => e
	      case _ => AffectedRows(1)
	    }
	  }
	  
	  case DropTable(tbName) => {
	    val maybeTable = Tables.drop(tbName)
	    maybeTable match {
	      case Left(e) => e
	      case _ => AffectedRows(1)
	    }
	  }
	  
	  case ShowDatabases() => {
	    val currentDBName = Databases.current.fold("")(_.name)
	    
	    val dbNames = Databases.dbList.map(db => {
	    	if (db.name == currentDBName)
	    	  db.name + " [*]"
	    	else
    	      db.name
	    })
	    
	    println(dbNames.mkString("\n"))
	    AffectedRows(0)
	  }
	  
	  case ShowTables() => {
	    
	    val maybeTables = Tables.tbList
	    maybeTables match {
	      case Left(e)=> e
	      case Right(theList)=> {
	        val tbNames = theList.map(db => db.name)	        
		    println(tbNames.mkString("\n"))
		    AffectedRows(0)
	      }
	    }
	  }
	  
	  case ShowColumns(tableName) => {
	    
	    val maybeCols = Tables.getCols(tableName)
	    maybeCols match {
	      case Left(e) => e
	      case Right(colList) => {
	        val colNames = colList.map(col => col.name)
	        println(colNames.mkString("\n"))
	        AffectedRows(0)
	      }
	    }	    
	  }
	  
	  case DropDatabase(dbname) => {
	    val success = AffectedRows(1)
	    Databases.drop(dbname).fold[ExecutionResult](DATABASE_DOES_NOT_EXIST(dbname))(_ => success)
	  }
	  
	  case RenameTable(tbName, newName) => Tables.rename(tbName, newName) match{
	    case Right(db) => AffectedRows(1)
	    case Left(e) => e
	  }
	  
	  case UseDatabase(dbname) => Databases.use(dbname) match{
	    case Some(db) => AffectedRows(0)
	    case _ => DATABASE_DOES_NOT_EXIST(dbname)
	  }
	  
	  case AlterDatabase(oldName, newName) => Databases.alter(oldName, newName) match{
	    case Right(db) => AffectedRows(1)
	    case Left(e) => e
	  }

	  case c => Error("Not implemented yet: "+c)
	}
}