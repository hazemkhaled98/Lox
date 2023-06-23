package jlox.parser;

import jlox.scanner.Token;

import java.util.List;

public abstract class Expr {
	public abstract <T> T accept(Visitor<T> visitor);

	public interface Visitor<T> {
		T visitBinaryExpr (Binary expr);
		T visitGroupingExpr (Grouping expr);
		T visitLiteralExpr (Literal expr);
		T visitUnaryExpr (Unary expr);
		T visitVariableExpr(Variable expr);
		T visitAssignExpr(Assign expr);
		T visitLogicalExpr(Logical expr);
		T visitCallExpr(Call expr);
		T visitGetExpr(Get expr);
		T visitSetExpr(Set expr);
		T visitThisExpr(This expr);
		T visitSuperExpr(Super expr);
	}
	public static class Binary extends Expr {

		@Override
		public <T> T accept(Visitor<T> visitor) {
			return visitor.visitBinaryExpr(this);
		}

		private final Expr left;
		private final Token operator;
		private final Expr right;
		Binary(Expr left, Token operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		public Expr getLeft() {
			return left;
		}

		public Token getOperator() {
			return operator;
		}

		public Expr getRight() {
			return right;
		}
	}
	public static class Grouping extends Expr {

		@Override
		public <T> T accept(Visitor<T> visitor) {
			return visitor.visitGroupingExpr(this);
		}

		private final Expr expression;
		Grouping (Expr expression) {
			this.expression = expression;
		}
		public Expr getExpression(){
			return expression;
		}
	}
	public static class Literal extends Expr {

		@Override
		public <T> T accept(Visitor<T> visitor) {
			return visitor.visitLiteralExpr(this);
		}

		private final Object value;
		Literal (Object value) {
			this.value = value;
		}
		public Object getValue(){
			return value;
		}
	}
	public static class Unary extends Expr {

		@Override
		public <T> T accept(Visitor<T> visitor) {
			return visitor.visitUnaryExpr(this);
		}

		private final Token operator;
		private final Expr right;
		Unary (Token operator, Expr right) {
			this.operator = operator;
			this.right = right;
		}

		public Expr getRight(){
			return right;
		}

		public Token getOperator(){
			return operator;
		}
	}

	public static class Variable extends Expr{

		public <T> T accept(Visitor<T> visitor){
			return visitor.visitVariableExpr(this);
		}

		private final Token name;
		public Variable(Token name){
			this.name = name;
		}

		public Token getName(){
			return name;
		}
	}

	public static class Assign extends Expr{

		@Override
		public <T> T accept(Visitor<T> visitor) {
			return visitor.visitAssignExpr(this);
		}

		private final Token name;
		private final Expr value;

		public Assign(Token name, Expr value){
			this.name = name;
			this.value = value;
		}

		public Token getName() {
			return name;
		}

		public Expr getValue() {
			return value;
		}
	}

	public static class Logical extends Expr{
		public <T> T accept(Visitor<T> visitor){
			return visitor.visitLogicalExpr(this);
		}

		private final Expr left;
		private final Token operator;
		private final Expr right;

		public Logical(Expr left, Token operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		public Expr getLeft() {
			return left;
		}

		public Token getOperator() {
			return operator;
		}

		public Expr getRight() {
			return right;
		}
	}

	public static class Call extends Expr{
		public <T> T accept(Visitor<T> visitor){
			return visitor.visitCallExpr(this);
		}

		private final Expr callee;
		private final Token paren;
		private final List<Expr> arguments;

		public Call(Expr callee, Token paren, List<Expr> arguments) {
			this.callee = callee;
			this.paren = paren;
			this.arguments = arguments;
		}

		public Expr getCallee() {
			return callee;
		}

		public Token getParen() {
			return paren;
		}

		public List<Expr> getArguments() {
			return arguments;
		}
	}

	public static class Get extends Expr{
		@Override
		public <T> T accept(Visitor<T> visitor) {
			return visitor.visitGetExpr(this);
		}

		private final Expr object;
		private final Token name;

		public Get(Expr object, Token name) {
			this.object = object;
			this.name = name;
		}

		public Expr getObject() {
			return object;
		}

		public Token getName() {
			return name;
		}
	}

	public static class Set extends Expr{
		@Override
		public <T> T accept(Visitor<T> visitor) {
			return visitor.visitSetExpr(this);
		}

		private final Expr object;
		private final Token name;
		private final Expr value;

		public Set(Expr object, Token name, Expr value) {
			this.object = object;
			this.name = name;
			this.value = value;
		}

		public Expr getObject() {
			return object;
		}

		public Token getName() {
			return name;
		}

		public Expr getValue() {
			return value;
		}
	}

	public static class This extends Expr{
		@Override
		public <T> T accept(Visitor<T> visitor) {
			return visitor.visitThisExpr(this);
		}

		private final Token keyword;

		public This(Token keyword) {
			this.keyword = keyword;
		}

		public Token getKeyword() {
			return keyword;
		}
	}

	public static class Super extends Expr{
		@Override
		public <T> T accept(Visitor<T> visitor) {
			return visitor.visitSuperExpr(this);
		}

		private final Token keyword;
		private final Token method;

		public Super(Token keyword, Token method) {
			this.keyword = keyword;
			this.method = method;
		}

		public Token getKeyword() {
			return keyword;
		}

		public Token getMethod() {
			return method;
		}
	}
}
